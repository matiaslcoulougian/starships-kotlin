package factories

import javafx.scene.input.KeyCode
import ui.STAGE_HEIGHT
import ui.STAGE_WIDTH
import models.*
import persistance.loadConfigs
import utils.Configuration
import utils.KeyBindingConfig
import utils.getRandomDouble
import utils.getRandomInt
import java.util.*

class ClassicGameStateFactory: GameStateFactory {
    override fun buildGame(): GameState {
        val configurations = loadConfigs()
        //create move and movables list
        val movables: MutableList<Movable> = createStarshipsList(configurations)
        val scoreBoard = ScoreBoard(generateScoreBoardMap(movables))
        return GameState(movables, listOf(), GameStatus.PLAY, 0.0, 0.0, scoreBoard, createGameConfig(configurations, movables), 0.0)
    }

    private fun createGameConfig(
        configurations: Configuration,
        movables: MutableList<Movable>
    ) = GameConfig(
        configurations.playersAmount,
        configurations.winningPoints,
        configurations.gameTime,
        createKeyBindingMap(movables.filterIsInstance<Starship>(), configurations.keyBinding),
        configurations.asteroidSpawnRate,
        configurations.asteroidsDamage,
        configurations.boosterSpawnRate
    )

    private fun generateScoreBoardMap(movables: MutableList<Movable>): MutableMap<String, Int> {
        //create scoreboard
        val scoreBoardMap: MutableMap<String, Int> = mutableMapOf()
        movables.forEach {
            scoreBoardMap[it.getId()] = 0
        }
        return scoreBoardMap
    }

    private fun createStarshipsList(configurations: Configuration): MutableList<Movable> {
        val mover = Mover()
        val movables: MutableList<Movable> = mutableListOf()
        //add starships from config
        for (i in 0 until configurations.playersAmount) {
            val weapon = buildWeapon(configurations.bullets[i].damage, configurations.bullets[i].speed, getBulletColor(configurations.bullets[i].color))
            val starship = buildStarship( configurations.starshipsLife, weapon, 5.0, 0.5, mover, Position(getRandomDouble(20, STAGE_WIDTH.toInt() - 20), getRandomDouble(20, STAGE_HEIGHT.toInt() - 20)), configurations.playersNames[i], configurations.starships[i].type)
            movables.add(starship)
        }
        return movables
    }

    fun buildStarship(life: Int, weapon: Weapon, maxSpeed: Double, acceleration: Double, mover: Mover, position: Position, name: String, type: Int): Starship {
        return Starship("starship_" + UUID.randomUUID().toString(), name, life, weapon, position, maxSpeed, acceleration, 0.0, 0.0 ,mover, type)
    }

    fun buildAsteroid(damage: Int, life: Int, position: Position, mover: Mover, speed: Double, rotation: Double, type: Int): Asteroid{
        return Asteroid("asteroid_" + UUID.randomUUID().toString(), damage, life, position, speed, rotation, mover, type)
    }

    override fun buildRandomAsteroid(damage: Int, randomInt: Int, life: Int, speed: Double, type: Int): Asteroid {
        val position = this.createRandomBorderPosition(randomInt)
        val rotation = getRandomRotation(randomInt)
        return this.buildAsteroid(damage, life, position, Mover(), speed, rotation, type)
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

    override fun buildRandomBooster(xPosition: Double, yPosition: Double): Booster = buildBooster(BoosterType.HEALTH, Position(xPosition, yPosition))

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