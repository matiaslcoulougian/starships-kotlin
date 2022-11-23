package services

import factories.GameStateFactory
import javafx.scene.input.KeyCode
import models.*

data class GameEngine(
    val gameState: GameState,
    val gameStateFactory: GameStateFactory
) {
    fun handleCollision(element1Id: String, element2Id: String): GameEngine {
        val movable1 = gameState.movables.find { it.getId() == element1Id }
        val movable2 = gameState.movables.find { it.getId() == element2Id }
        val movables = gameState.movables.toMutableList()
        movables.remove(movable1)
        movables.remove(movable2)
        return if (movable1 != null && movable2 != null) {
            var collidable1 = movable1 as Collidable
            var collidable2 = movable2 as Collidable
            collidable1 = collidable1.collide(collidable2)
            collidable2 = collidable2.collide(collidable1)
            if (collidable1.getLife() > 0) movables.add(collidable1 as Movable)
            if (collidable2.getLife() > 0) movables.add(collidable2 as Movable)
            val newScore = checkAndUpdateScore(collidable1, collidable2)
            copy(gameState = gameState.copy(movables = movables, scoreBoard = newScore))
        } else this
    }

    fun handleKeyPressed(key: KeyCode): GameEngine {
        val starshipAction = this.getStarshipIdForKey(key);
        if (starshipAction != null) {
            return when (starshipAction.second) {
                Action.SHOOT -> this.shoot(starshipAction.first)
                Action.ROTATE_CLOCKWISE -> this.rotate(starshipAction.first, 15)
                Action.ROTATE_ANTICLOCKWISE -> this.rotate(starshipAction.first, -15)
                Action.ACCELERATE -> this.accelerate(starshipAction.first)
                Action.DECELERATE -> this.decelerate(starshipAction.first)
            }
        }
        return this
    }

    fun handleTimePassed(currentTime: Double, secondsSinceLastTime: Double): GameEngine {
        val initialTime = if (gameState.initialTime  == 0.0) currentTime else gameState.initialTime
        val isGameOver = checkGameOver(currentTime)
        if (isGameOver && gameState.initialTime != 0.0) return copy(gameState = gameState.copy(status = GameStatus.OVER))
        val movables = gameState.movables.toMutableList()
        var timeSinceLastAsteroid = gameState.timeSinceLastAsteroid
        if (currentTime - timeSinceLastAsteroid > 1.5) {
            movables.add(gameStateFactory.buildRandomAsteroid())
            timeSinceLastAsteroid = currentTime
        }
        return copy(gameState = gameState.copy(movables = movables.map {
            it.move(secondsSinceLastTime)
        }, timeSinceLastAsteroid = timeSinceLastAsteroid, initialTime = initialTime))
    }

    fun handleElementOutOfBounds(id: String): GameEngine {
        val movable = gameState.movables.find { it.getId() == id }
        if (movable != null && movable !is Starship) {
            val movables = gameState.movables.toMutableList()
            movables.remove(movable)
            return copy(gameState = gameState.copy(movables = movables))
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

    fun handlePauseAndResume(): GameEngine {
        return if (gameState.status === GameStatus.PLAY) copy(gameState = gameState.copy(status = GameStatus.PAUSE))
        else copy(gameState = gameState.copy(status = GameStatus.PLAY))
    }

    private fun accelerate(starshipId: String): GameEngine {
        var starship = gameState.movables.find { it.getId() == starshipId }
        if (starship != null) {
            val movables = gameState.movables.toMutableList()
            movables.remove(starship)
            starship = (starship as Starship).accelerate()
            movables.add(starship)
            return copy(gameState = gameState.copy(movables = movables))
        }
        return this
    }

    private fun decelerate(starshipId: String): GameEngine {
        var starship = gameState.movables.find { it.getId() == starshipId }
        if (starship != null) {
            val movables = gameState.movables.toMutableList()
            movables.remove(starship)
            starship = (starship as Starship).decelerate()
            movables.add(starship)
            return copy(gameState = gameState.copy(movables = movables))
        }
        return this
    }

    private fun shoot(starshipId: String): GameEngine {
        val starship = gameState.movables.find { it.getId() == starshipId }
        val bullets = gameState.movables.filterIsInstance<Bullet>()
        if (starship != null && bullets.size <= 10) {
            val movables = gameState.movables.toMutableList()
            val bullet = gameStateFactory.buildBullet((starship as Starship).getWeapon().damage, starship.getPosition(), starship.getWeapon().shootSpeed, starship.getRotation(), starship.getMover(), starshipId, starship.getWeapon().bulletColor)
            movables.add(bullet)
            return copy(gameState = gameState.copy(movables = movables))
        }
        return this
    }

    private fun rotate(starshipId: String, degrees: Int): GameEngine {
        var starship = gameState.movables.find { it.getId() == starshipId }
        if (starship != null) {
            val movables = gameState.movables.toMutableList()
            movables.remove(starship)
            starship = (starship as Starship).rotate(degrees)
            movables.add(starship)
            return copy(gameState = gameState.copy(movables = movables))
        }
        return this
    }

    private fun getStarshipIdForKey(key: KeyCode): Pair<String, Action>? {
         gameState.keyBinding.forEach{
            val (starshipId, keysToAction) = it
            val action = keysToAction[key]
            if (action != null) return Pair(starshipId, action)
        }
        return null
    }

    private fun checkAndUpdateScore(collidable1: Collidable, collidable2: Collidable): ScoreBoard {
        return if (collidable1 is Bullet && collidable2 is Asteroid && collidable2.getLife() < 0) {
            gameState.scoreBoard.addPoints(collidable1.getStarshipId(), 10)
        } else if (collidable2 is Bullet && collidable1 is Asteroid && collidable1.getLife() < 0) {
            gameState.scoreBoard.addPoints(collidable2.getStarshipId(), 10)
        } else gameState.scoreBoard
    }

    fun getWinner(): Starship? {
        val remainingStarships = gameState.movables.filterIsInstance<Starship>()
        return if (gameState.gameConfig.playersAmount > 1) {
            return if (remainingStarships.size == 1) remainingStarships[0]
            else if (remainingStarships.size > 1) {
                var winningScore = Pair("", 0)
                gameState.scoreBoard.getScore().filter {
                    val (_, points) = it
                    points >= gameState.gameConfig.winningPoints
                }.forEach {
                    val (starshipId, points) = it
                    if (points > winningScore.second) winningScore = Pair(starshipId, points)
                }
                remainingStarships.find { it.getId() == winningScore.first }
            }
            else null
        }
        else {
            if (remainingStarships.size == 1) {
                val starship = remainingStarships[0]
                val score = gameState.scoreBoard.getScore()[starship.getId()]
                if (score != null && score >= gameState.gameConfig.winningPoints) starship
                else null
            }
            else null
        }
    }


    private fun checkGameOver(currentTime: Double): Boolean {
        if (currentTime - gameState.initialTime > gameState.gameConfig.gameTime) return true
        val winningScores = gameState.scoreBoard.getScore().values.find { it >= gameState.gameConfig.winningPoints }
        return if (gameState.gameConfig.playersAmount == 1) gameState.movables.filterIsInstance<Starship>().isEmpty() || winningScores != null && winningScores > 0
        else gameState.movables.filterIsInstance<Starship>().size <= 1 || winningScores != null && winningScores > 0
    }

}