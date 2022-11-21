package factories

import javafx.scene.input.KeyCode
import main.STAGE_HEIGHT
import main.STAGE_WIDTH
import models.*
import utils.getRandomDouble
import utils.getRandomInt
import java.util.*
import kotlin.collections.HashMap

class ClassicGameStateFactory: GameStateFactory {
    override fun buildGame(): GameState {
        val mover = Mover();
        val starship1: Starship = buildStarship(1000, buildWeapon(), 50.0, 0.5, mover, Position(370.0, 400.0))
        val starship2: Starship = buildStarship(1000, buildWeapon(), 50.0, 0.5, mover, Position(430.0, 400.0))
        val asteroid = buildAsteroid(5, 50, Position(1.0, 1.0), mover, 3.0, 80.0)
        return GameState(listOf( starship1, starship2, asteroid), HashMap(mapOf(Pair(starship1.getId(), createKeyBinding1()), Pair(starship2.getId(), createKeyBinding2()))), GameStatus.PLAY, 0.0, ScoreBoard(HashMap()))
    }

    fun buildStarship(life: Int, weapon: Weapon, maxSpeed: Double, acceleration: Double, mover: Mover, position: Position): Starship {
        return Starship("starship_" + UUID.randomUUID().toString(), "Classic Starship", life, weapon, position, maxSpeed, acceleration, 0.0, 0.0 ,mover)
    }

    fun buildAsteroid(damage: Int, life: Int, position: Position, mover: Mover, speed: Double, rotation: Double): Asteroid{
        return Asteroid("asteroid_" + UUID.randomUUID().toString(), damage, life, position, speed, rotation, mover)
    }

    override fun buildRandomAsteroid(): Asteroid {
        val randomInt = getRandomInt(1, 4)
        val position = this.createRandomPosition(randomInt)
        val rotation = getRandomRotation(randomInt)
        return this.buildAsteroid(getRandomInt(10, 20), getRandomInt(70, 100), position, Mover(), getRandomDouble(1, 4), rotation)
    }

    fun buildWeapon(): Weapon {
        return Weapon(Stack<Bullet>() , 10, 8.0);
    }

    override fun buildBullet(damage: Int, position: Position, speed: Double, rotation: Double, mover: Mover, starshipId: String): Bullet {
        return Bullet("bullet_" + UUID.randomUUID().toString(), damage, 10, position.copy(x = position.getX() + 18), speed, rotation, mover, starshipId)
    }

    private fun createKeyBinding1(): Map<KeyCode, Action> {
        return HashMap(mapOf(
            Pair(KeyCode.UP, Action.ACCELERATE),
            Pair(KeyCode.DOWN, Action.DECELERATE),
            Pair(KeyCode.LEFT, Action.ROTATE_ANTICLOCKWISE),
            Pair(KeyCode.RIGHT, Action.ROTATE_CLOCKWISE),
            Pair(KeyCode.SPACE, Action.SHOOT)
        ))
    }

    private fun createKeyBinding2(): Map<KeyCode, Action> {
        return HashMap(mapOf(
            Pair(KeyCode.W, Action.ACCELERATE),
            Pair(KeyCode.S, Action.DECELERATE),
            Pair(KeyCode.A, Action.ROTATE_ANTICLOCKWISE),
            Pair(KeyCode.D, Action.ROTATE_CLOCKWISE),
            Pair(KeyCode.R, Action.SHOOT)
        ))
    }

    fun createRandomPosition (positionVariant: Int): Position {
        return when (positionVariant) {
            1 -> Position(getRandomDouble(0, STAGE_WIDTH.toInt()), 0.0)
            2 -> Position(getRandomDouble(0, STAGE_WIDTH.toInt()), STAGE_HEIGHT)
            3 -> Position(0.0, getRandomDouble(0, STAGE_HEIGHT.toInt()))
            4 ->  Position(STAGE_WIDTH, getRandomDouble(0, STAGE_HEIGHT.toInt()))
            else -> Position(0.0, 0.0)
        }
    }

    fun getRandomRotation(rotationVariant: Int): Double {
        return when (rotationVariant) {
            1 -> getRandomDouble(120, 140)
            2 -> if (getRandomInt(0,1) == 1) getRandomDouble(280, 340) else getRandomDouble(20, 70)
            3 -> getRandomDouble(20, 160)
            4 ->  getRandomDouble(190, 340)
            else -> 0.0
        }
    }


}