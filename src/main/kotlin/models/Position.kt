package models

data class Position(
    private val x: Double,
    private val y: Double
) {
    fun getX(): Double = this.x;
    fun getY(): Double = this.y;
}