package models

import java.util.*

data class Weapon(
    val bullets: Stack<Bullet>,
    val damage: Int,
    val shootSpeed: Double,
    val bulletColor: BulletColor
) {
}