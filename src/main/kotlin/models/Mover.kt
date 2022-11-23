package models

import kotlin.math.cos
import kotlin.math.sin

class Mover {
    fun moveMovable(movable: Movable, time: Double): Position {
        val x = movable.getPosition().getX() - (movable.getSpeed() * -sin(Math.toRadians(movable.getRotation())) * time * 50)
        val y = movable.getPosition().getY() - (movable.getSpeed() * cos(Math.toRadians(movable.getRotation())) * time * 50)
        return Position(x = x, y = y)
    }
}