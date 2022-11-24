package factories

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson

import javafx.scene.input.KeyCode
import ui.STAGE_HEIGHT
import ui.STAGE_WIDTH
import models.*
import persistance.loadConfigs
import utils.KeyBindingConfig
import utils.getRandomDouble
import utils.getRandomInt
import java.util.*
import kotlin.collections.HashMap

class ClassicGameStateFactory: GameStateFactory {
    override fun buildGame(): GameState {

        val configurations = loadConfigs()

        //create move and movables list
        val mover = Mover()
        val movables: MutableList<Movable> = mutableListOf()

        //add starships from config
        for (i in 0 until configurations.playersAmount) {
            movables.add(
                buildStarship(
                    configurations.starshipsLife,
                    buildWeapon(
                        configurations.bullets[i].damage,
                        configurations.bullets[i].speed,
                        getBulletColor(configurations.bullets[i].color)
                    ),
                    5.0,
                    0.5,
                    mover,
                    createRandomPosition(),
                    configurations.playersNames[i],
                    configurations.starships[i].type)
            )
        }

        //create scoreboard
        val scoreBoardMap: MutableMap<String, Int> = mutableMapOf()
        movables.forEach {
            scoreBoardMap[it.getId()] = 0
        }

//        val starship1: Starship = buildStarship(500, buildWeapon(8, 11.0, BulletColor.RED), 5.0, 0.5, mover, Position(370.0, 400.0), "Mati", 2)
//        val starship2: Starship = buildStarship(500, buildWeapon(8, 11.0, BulletColor.BLUE), 5.0, 0.5, mover, Position(430.0, 400.0), "NPC", 3)
        return GameState(
            movables,
            listOf(),
            GameStatus.PLAY,
            0.0,
            0.0,
            ScoreBoard(scoreBoardMap),
            GameConfig(
                configurations.playersAmount,
                configurations.winningPoints,
                configurations.gameTime,
                createKeyBindingMap(movables.filterIsInstance<Starship>(), configurations.keyBinding),
                configurations.asteroidSpawnRate,
                configurations.asteroidsDamage,
                configurations.boosterSpawnRate
            ),
            0.0
        )
    }

    fun buildStarship(life: Int, weapon: Weapon, maxSpeed: Double, acceleration: Double, mover: Mover, position: Position, name: String, type: Int): Starship {
        return Starship("starship_" + UUID.randomUUID().toString(), name, life, weapon, position, maxSpeed, acceleration, 0.0, 0.0 ,mover, type)
    }

    fun buildAsteroid(damage: Int, life: Int, position: Position, mover: Mover, speed: Double, rotation: Double, type: Int): Asteroid{
        return Asteroid("asteroid_" + UUID.randomUUID().toString(), damage, life, position, speed, rotation, mover, type)
    }

    override fun buildRandomAsteroid(damage: Int): Asteroid {
        val randomInt = getRandomInt(1, 4)
        val position = this.createRandomBorderPosition(randomInt)
        val rotation = getRandomRotation(randomInt)
        return this.buildAsteroid(damage, getRandomInt(40, 60), position, Mover(), getRandomDouble(1, 4), rotation, getRandomInt(1, 2))
    }

    fun buildWeapon(damage: Int, shotSpeed: Double, bulletColor: BulletColor): Weapon {
        return Weapon(Stack<Bullet>() , damage, shotSpeed, bulletColor);
    }

    override fun buildBullet(damage: Int, position: Position, speed: Double, rotation: Double, mover: Mover, starshipId: String, color: BulletColor): Bullet {
        return Bullet("bullet_" + UUID.randomUUID().toString(), damage, 10, position.copy(x = position.getX() + 18), speed, rotation, mover, starshipId, color)
    }

    fun buildBooster(type: BoosterType, position: Position): Booster {
        return Booster("booster_" + UUID.randomUUID().toString(), type , position, 1)
    }

    override fun buildRandomBooster(): Booster = buildBooster(BoosterType.HEALTH, createRandomPosition())

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
            Pair(KeyCode.Q, Action.SHOOT)
        ))
    }

    private fun createKeyBindingMap(starships: List<Starship>, keyBindingConfig: List<KeyBindingConfig>): Map<String, Map<KeyCode, Action>> {
        val keyBindings: MutableMap<String, Map<KeyCode, Action>> = mutableMapOf()
        keyBindingConfig.forEach {
            val starship = starships.find { starship -> starship.getName() == it.name }
            if (starship != null) {
                val keyCodes: MutableMap<KeyCode, Action> = mutableMapOf()
                it.keys.forEach {
                    keyBindingConfig -> keyCodes[KeyCode.valueOf(keyBindingConfig.value)] = Action.valueOf(keyBindingConfig.key)
                }
                keyBindings[starship.getId()] = keyCodes
            }
        }
        return keyBindings
    }

    fun createRandomBorderPosition (positionVariant: Int): Position {
        return when (positionVariant) {
            1 -> Position(getRandomDouble(0, STAGE_WIDTH.toInt()), 0.0)
            2 -> Position(getRandomDouble(0, STAGE_WIDTH.toInt()), STAGE_HEIGHT)
            3 -> Position(0.0, getRandomDouble(0, STAGE_HEIGHT.toInt()))
            4 ->  Position(STAGE_WIDTH, getRandomDouble(0, STAGE_HEIGHT.toInt()))
            else -> Position(0.0, 0.0)
        }
    }

    fun createRandomPosition (): Position = Position(getRandomDouble(20, STAGE_WIDTH.toInt() - 20), getRandomDouble(20, STAGE_HEIGHT.toInt() - 20))


    fun getRandomRotation(rotationVariant: Int): Double {
        return when (rotationVariant) {
            1 -> getRandomDouble(120, 140)
            2 -> if (getRandomInt(0,1) == 1) getRandomDouble(280, 340) else getRandomDouble(20, 70)
            3 -> getRandomDouble(20, 160)
            4 ->  getRandomDouble(190, 340)
            else -> 0.0
        }
    }

    fun getBulletColor(color: String): BulletColor {
        return when (color) {
            "BLUE" -> BulletColor.BLUE
            "RED" -> BulletColor.RED
            else -> BulletColor.BLUE
        }
    }

}