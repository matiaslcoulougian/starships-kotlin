package models

sealed interface Collidable {
    fun collide(collidable: Collidable): Collidable;
    fun getLife(): Int;

    fun isDead() = getLife() <= 0
    fun isAlive() = !isDead()
}