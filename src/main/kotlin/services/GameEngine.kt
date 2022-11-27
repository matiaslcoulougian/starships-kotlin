package services

import factories.GameStateFactory
import javafx.scene.input.KeyCode
import models.*
import ui.STAGE_HEIGHT
import ui.STAGE_WIDTH
import utils.getRandomDouble
import utils.getRandomInt

data class GameEngine(
    val gameState: GameState,
    val gameStateFactory: GameStateFactory
) {
    fun handleCollision(element1Id: String, element2Id: String): GameEngine {
        val movable1 = gameState.movables.find { it.getId() == element1Id }
        val movable2 = gameState.movables.find { it.getId() == element2Id }
        val booster1 = gameState.boosters.find { it.getId() == element1Id }
        val booster2 = gameState.boosters.find { it.getId() == element2Id }

        return if (movable1 != null && movable2 != null) {
            handleMovablesCollision(movable1, movable2)
        }
        else if (booster1 != null && movable2 != null) {
            handleBoosterCollision(movable2, booster1)
        }
        else if (booster2 != null && movable1 != null) {
            handleBoosterCollision(movable1, booster2)
        }
        else this
    }

    private fun handleBoosterCollision(movable: Movable, booster: Booster): GameEngine {
        val movables = gameState.movables.toMutableList()
        val boosters = gameState.boosters.toMutableList()
        movables.remove(movable)
        boosters.remove(booster)
        var collidableMovable = movable as Collidable
        var collidableBooster = booster as Collidable
        collidableMovable = collidableMovable.collide(collidableBooster)
        collidableBooster = collidableBooster.collide(collidableMovable)
        if (collidableBooster.isAlive()) boosters.add(collidableBooster as Booster)
        if (collidableMovable.isAlive()) movables.add(collidableMovable as Movable)
        return copy(gameState = gameState.copy(movables = movables, boosters = boosters))
    }

    private fun handleMovablesCollision(movable1: Movable, movable2: Movable): GameEngine {
        val movables = gameState.movables.toMutableList()
        movables.remove(movable1)
        movables.remove(movable2)
        var collidable1 = movable1 as Collidable
        var collidable2 = movable2 as Collidable
        collidable1 = collidable1.collide(collidable2)
        collidable2 = collidable2.collide(collidable1)
        if (collidable1.isAlive()) movables.add(collidable1 as Movable)
        if (collidable2.isAlive()) movables.add(collidable2 as Movable)
        val newScore = gameState.scoreBoard.checkAndUpdateScores(collidable1, collidable2)
        return copy(gameState = gameState.copy(movables = movables, scoreBoard = newScore))
    }

    fun handleKeyPressed(key: KeyCode, secondsSinceLastTime: Double): GameEngine {
        val starshipAction = this.getStarshipIdForKey(key)
        if (starshipAction != null) {
            return when (starshipAction.second) {
                Action.ROTATE_CLOCKWISE -> this.rotate(starshipAction.first, 270 * secondsSinceLastTime )
                Action.ROTATE_ANTICLOCKWISE -> this.rotate(starshipAction.first, -270 * secondsSinceLastTime)
                Action.ACCELERATE -> this.accelerate(starshipAction.first)
                Action.DECELERATE -> this.decelerate(starshipAction.first)
                else -> this
            }
        }
        return this
    }

    fun handleShoot(key: KeyCode): GameEngine {
        val starshipAction = this.getStarshipIdForKey(key)
        return if (starshipAction != null && starshipAction.second == Action.SHOOT) {
            this.shoot(starshipAction.first)
        }
        else this
    }

    fun handleTimePassed(currentTime: Double, secondsSinceLastTime: Double): GameEngine {
        val initialTime = if (gameState.initialTime  == 0.0) currentTime else gameState.initialTime
        val isGameOver = checkGameOver(currentTime)
        if (isGameOver && gameState.initialTime != 0.0) return copy(gameState = gameState.copy(status = GameStatus.OVER))
        val movables = gameState.movables.toMutableList()
        val boosters = gameState.boosters.toMutableList()
        val timeSinceLastAsteroid = spawnAsteroid(gameState.timeSinceLastAsteroid, movables, secondsSinceLastTime)
        val timeSinceLastBooster = spawnBooster(gameState.timeSinceLastBooster, boosters, secondsSinceLastTime)
        return copy(gameState = gameState.copy(
            movables = movables.map { it.move(secondsSinceLastTime) },
            timeSinceLastAsteroid = timeSinceLastAsteroid,
            initialTime = initialTime,
            boosters = boosters,
            timeSinceLastBooster = timeSinceLastBooster
            )
        )
    }

    private fun spawnBooster(timeSinceLastBooster: Double, boosters: MutableList<Booster>, secondsSinceLastTime: Double): Double {
        return if (timeSinceLastBooster > gameState.gameConfig.boosterSpawnRate) {
            boosters.add(
                gameStateFactory.buildRandomBooster(
                    getRandomDouble(20, STAGE_WIDTH.toInt() - 20),
                    getRandomDouble(20, STAGE_HEIGHT.toInt() - 20)
                )
            )
            0.0
        } else timeSinceLastBooster + secondsSinceLastTime
    }

    private fun spawnAsteroid(timeSinceLastAsteroid: Double, movables: MutableList<Movable>, secondsSinceLastTime: Double): Double {
        return if (timeSinceLastAsteroid > gameState.gameConfig.asteroidSpawnRate) {
            movables.add(
                gameStateFactory.buildRandomAsteroid(
                    gameState.gameConfig.asteroidsDamage,
                    getRandomInt(1, 4),
                    getRandomInt(40, 60),
                    getRandomDouble(1, 4),
                    getRandomInt(1, 2)
                )
            )
            0.0
        } else timeSinceLastAsteroid + secondsSinceLastTime
    }

    fun handleElementOutOfBounds(id: String): GameEngine {
        val movable = gameState.movables.find { it.getId() == id }
        if (movable != null) {
            if (movable !is Starship || movable.getSpeed() != 0.0) {
                val movables = gameState.movables.toMutableList()
                movables.remove(movable)
                return copy(gameState = gameState.copy(movables = movables))
            }
        }
        return this
    }

    fun handleReachedBounds(id: String): GameEngine {
        var movable = gameState.movables.find { it.getId() == id }
        if (movable != null && movable is Starship) {
            val movables = gameState.movables.toMutableList()
            movables.remove(movable)
            movable = movable.stop()
            movables.add(movable)
            return copy(gameState = gameState.copy(movables = movables))
        }
        return this
    }

    fun handlePauseAndResume(pauseTimePassed: Double): GameEngine {
        return if (gameState.status === GameStatus.PLAY) copy(gameState = gameState.copy(status = GameStatus.PAUSE))
        else copy(gameState = gameState.copy(status = GameStatus.PLAY, initialTime = gameState.initialTime + pauseTimePassed))
    }

    private fun withStarship(id: String, block: (Starship) -> GameEngine): GameEngine {
        return when(val maybeStarship = gameState.movables.find { it.getId() == id }) {
            is Starship -> block(maybeStarship)
            else -> this
        }
    }

    private fun accelerate(starshipId: String): GameEngine {
        return withStarship(starshipId) { starship ->
            accelerate(starship)
        }
    }

    private fun accelerate(starship: Starship): GameEngine {
        val movables = gameState.movables.toMutableList()
        movables.remove(starship)
        val nextStarship = starship.accelerate()
        movables.add(nextStarship)
        return copy(gameState = gameState.copy(movables = movables))
    }

    private fun decelerate(starshipId: String): GameEngine {
        return withStarship(starshipId) {starship ->
            decelerate(starship)
        }
    }

    private fun decelerate(starship: Starship): GameEngine {
        val movables = gameState.movables.toMutableList()
        movables.remove(starship)
        val nextStarship = starship.decelerate()
        movables.add(nextStarship)
        return copy(gameState = gameState.copy(movables = movables))
    }

    private fun shoot(starshipId: String): GameEngine {
        return withStarship(starshipId) { starship ->
            shoot(starship)
        }
    }

    private fun shoot(starship: Starship): GameEngine {
        val movables = gameState.movables.toMutableList()
        val bullet = gameStateFactory.buildBullet(starship.getWeapon().damage, starship.getPosition(), starship.getWeapon().shootSpeed, starship.getRotation(), starship.getMover(), starship.getId(), starship.getWeapon().bulletColor)
        movables.add(bullet)
        return copy(gameState = gameState.copy(movables = movables))
    }

    private fun rotate(starshipId: String, degrees: Double): GameEngine {
       return withStarship(starshipId) { starship ->
            rotate(starship, degrees)
        }
    }

    private fun rotate(starship: Starship, degrees: Double): GameEngine {
        val movables = gameState.movables.toMutableList()
        movables.remove(starship)
        val next = starship.rotate(degrees)
        movables.add(next)
        return copy(gameState = gameState.copy(movables = movables))
    }

    private fun getStarshipIdForKey(key: KeyCode): Pair<String, Action>? {
         gameState.gameConfig.keyBinding.forEach{
            val (starshipId, keysToAction) = it
            val action = keysToAction[key]
            if (action != null) return Pair(starshipId, action)
        }
        return null
    }

    fun getWinner(): Starship? {
        val remainingStarships = gameState.movables.filterIsInstance<Starship>()
        return if (gameState.gameConfig.playersAmount > 1) getWinStatusForMoreThanOnePlayer(remainingStarships)
        else getWinStatusForOnePlayer(remainingStarships)
    }

    private fun getWinStatusForOnePlayer(remainingStarships: List<Starship>) =
        if (remainingStarships.size == 1) {
            val starship = remainingStarships[0]
            val score = gameState.scoreBoard.getScore()[starship.getId()]
            if (score != null && score >= gameState.gameConfig.winningPoints) starship
            else null
        } else null

    private fun getWinStatusForMoreThanOnePlayer(remainingStarships: List<Starship>): Starship? {
        return if (remainingStarships.size == 1) remainingStarships[0]
        else if (remainingStarships.size > 1) getWinnerStarship(remainingStarships)
        else null
    }

    private fun getWinnerStarship(remainingStarships: List<Starship>): Starship? {
        var winningScore = Pair("", 0)
        gameState.scoreBoard.getScore().filter {
            val (_, points) = it
            points >= gameState.gameConfig.winningPoints
        }.forEach {
            val (starshipId, points) = it
            if (points > winningScore.second) winningScore = Pair(starshipId, points)
        }
        return remainingStarships.find { it.getId() == winningScore.first }
    }


    private fun checkGameOver(currentTime: Double): Boolean {
        if (currentTime - gameState.initialTime > gameState.gameConfig.gameTime) return true
        val winningScores = gameState.scoreBoard.getScore().values.find { it >= gameState.gameConfig.winningPoints }
        return if (gameState.gameConfig.playersAmount == 1) gameState.movables.filterIsInstance<Starship>().isEmpty() || winningScores != null && winningScores > 0
        else gameState.movables.filterIsInstance<Starship>().size <= 1 || winningScores != null && winningScores > 0
    }

}