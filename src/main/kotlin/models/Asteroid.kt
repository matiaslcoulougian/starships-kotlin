package models

data class Asteroid(
    private val id: String,
    private val damage: Int,
    private val life: Int,
    private val position: Position,
    private val speed: Double,
    private val rotation: Double,
    private val mover: Mover,
    private val type: Int
): Collidable, Movable {
    fun getDamage(): Int = this.damage;
    override fun getSpeed(): Double = this.speed
    override fun getRotation(): Double = this.rotation
    override fun getPosition(): Position = this.position
    override fun move(time: Double): Movable {
        val newPosition = mover.moveMovable(this, time)
        return copy(position = newPosition)
    }
    override fun collide(collidable: Collidable): Collidable {
        return when (collidable) {
            is Asteroid -> copy(life = life - collidable.getDamage())
            is Starship -> copy(life = 0)
            is Bullet -> copy(life = life - collidable.getDamage())
            else -> this
        }
    }
    override fun getLife(): Int = life
    override fun getId(): String = id

    fun getType(): Int = type
}