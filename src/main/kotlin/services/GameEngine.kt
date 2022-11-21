package services

import adapters.GameModelToUIAdapter
import edu.austral.ingsis.starships.ui.ElementModel
import factories.GameStateFactory
import javafx.scene.input.KeyCode
import models.*

data class GameEngine(
    val gameState: GameState,
    val adapter: GameModelToUIAdapter,
    val gameStateFactory: GameStateFactory
) {
    fun addElements(elements: MutableMap<String, ElementModel>) {
        gameState.movables.forEach {
            if (elements[it.getId()] == null) {
                when (it) {
                    is Starship -> elements[it.getId()] = adapter.adaptStarship(it)
                    is Asteroid -> elements[it.getId()] = adapter.adaptAsteroid(it)
                    is Bullet -> elements[it.getId()] = adapter.adaptBullet(it)
                }
            }
        }
    }

    fun updateElementsPosition(elements: Map<String, ElementModel>) {
        gameState.movables.forEach {
            val element = elements[it.getId()]
            if (element != null) {
                element.x.set(it.getPosition().getX())
                element.y.set(it.getPosition().getY())
                element.rotationInDegrees.set(it.getRotation())
                //if (it is Asteroid && it.getLife() < 0) element.image.set(SimpleObjectProperty(EXPLODED_ASTEROID))
            }
        }
    }

    fun removeElements(elements: MutableMap<String, ElementModel>) {
        val toRemove = elements.keys.filter { !gameState.movables.map { movable -> movable.getId() }.contains(it) }
        toRemove.forEach { elements.remove(it) }
    }


    fun handleCollision(element1Id: String, element2Id: String): GameEngine {
        val movable1 = gameState.movables.find { it.getId() == element1Id }
        val movable2 = gameState.movables.find { it.getId() == element2Id }
        val movables = gameState.movables.toMutableList()
        movables.remove(movable1)
        movables.remove(movable2)
        var collidable1 = movable1 as Collidable
        var collidable2 = movable2 as Collidable
        collidable1 = collidable1.collide(collidable2)
        collidable2 = collidable2.collide(collidable1)
        if (collidable1.getLife() > 0) movables.add(collidable1 as Movable)
        if (collidable2.getLife() > 0) movables.add(collidable2 as Movable)
        val newScore = checkAndUpdateScore(collidable1, collidable2)
        return copy(gameState = gameState.copy(movables = movables, scoreBoard = newScore))
    }

    fun handleKeysPressed(keys: Set<KeyCode>): GameEngine {
        var gameEngine = this
        for (key in keys) {
            val starshipAction = this.getStarshipIdForKey(key);
            if (starshipAction != null) {
                gameEngine = when (starshipAction.second) {
                    Action.SHOOT -> this.shoot(starshipAction.first)
                    Action.ROTATE_CLOCKWISE -> this.rotate(starshipAction.first, 15)
                    Action.ROTATE_ANTICLOCKWISE -> this.rotate(starshipAction.first, -15)
                    Action.ACCELERATE -> this.accelerate(starshipAction.first)
                    Action.DECELERATE -> this.decelerate(starshipAction.first)
                }
            }
        }
        return gameEngine
    }

    fun handleTimePassed(currentTime: Double, secondsSinceLastTime: Double): GameEngine {
        val movables = gameState.movables.toMutableList()
        var timeSinceLastAsteroid = gameState.timeSinceLastAsteroid
        if (currentTime - timeSinceLastAsteroid > 1.5) {
            movables.add(gameStateFactory.buildRandomAsteroid())
            timeSinceLastAsteroid = currentTime
        }
        return copy(gameState = gameState.copy(movables = movables.map {
            it.move(currentTime - secondsSinceLastTime)
        }, timeSinceLastAsteroid = timeSinceLastAsteroid))
    }

    fun handleElementOutOfBounds(id: String): GameEngine {
        val movable = gameState.movables.find { it.getId() == id }
        if (movable != null) {
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
        if (starship != null) {
            val movables = gameState.movables.toMutableList()
            val bullet = gameStateFactory.buildBullet((starship as Starship).getWeapon().damage, starship.getPosition(), starship.getWeapon().shootSpeed, starship.getRotation(), starship.getMover(), starshipId)
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


}