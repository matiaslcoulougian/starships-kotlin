package factories

import models.*

sealed interface GameStateFactory {
    fun buildGame(): GameState
    fun buildBullet(damage: Int, position: Position, speed: Double, rotation: Double, mover: Mover, starshipId: String): Bullet
    fun buildRandomAsteroid(): Asteroid
}