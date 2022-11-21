package models

sealed interface Movable {
    fun move(time: Double): Movable
    fun getPosition(): Position
    fun getSpeed(): Double
    fun getRotation(): Double
    fun getId(): String
}