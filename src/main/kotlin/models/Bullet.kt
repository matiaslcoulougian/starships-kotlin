package models

data class Bullet(
    private val id: String,
    private val damage: Int,
    private val life: Int,
    private val position: Position,
    private val speed: Double,
    private val rotation: Double,
    private val mover: Mover,
    private val starshipId: String,
    private val color: BulletColor
): Collidable, Movable {
    override fun getSpeed(): Double = this.speed
    override fun getRotation(): Double = this.rotation
    override fun getPosition(): Position = this.position
    override fun move(time: Double): Movable {
        val newPosition = mover.moveMovable(this, time)
        return copy(position = newPosition)
    }
    override fun collide(collidable: Collidable): Collidable {
        return when (collidable) {
            is Asteroid -> copy(life = 0)
            is Starship -> if (collidable.getId() != starshipId) copy(life = 0) else this
            is Bullet -> this
            is Booster -> copy(life = 0)
            else -> this
        }
    }
    override fun getLife(): Int = life
    fun getDamage(): Int = damage
    override fun getId(): String = id
    fun getStarshipId(): String = starshipId
    fun getColor(): BulletColor = color
}