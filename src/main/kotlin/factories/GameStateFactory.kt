package factories

import models.*

sealed interface GameStateFactory {
    fun buildGame(): GameState
    fun buildBullet(damage: Int, position: Position, speed: Double, rotation: Double, mover: Mover, starshipId: String, color: BulletColor): Bullet
    fun buildRandomAsteroid(damage: Int, randomInt: Int, life: Int, speed: Double, type: Int): Asteroid
    fun buildRandomBooster(xPosition: Double, yPosition: Double): Booster
}