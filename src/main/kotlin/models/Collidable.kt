package models

sealed interface Collidable {
    fun collide(collidable: Collidable): Collidable;
    fun getLife(): Int;
}