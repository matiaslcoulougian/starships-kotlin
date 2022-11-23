package models

data class Starship(
    private val id: String,
    private val name: String,
    private val life: Int,
    private val weapon: Weapon,
    private val position: Position,
    private val maxSpeed: Double,
    private val acceleration: Double,
    private val speed: Double,
    private val rotation: Double,
    private val mover: Mover,
    private val type: Int
    ): Movable, Collidable {
    fun accelerate(): Starship {
        return if (speed < maxSpeed) {
            copy(speed = speed + acceleration)
        } else {
            this
        }
    }

    fun decelerate(): Starship {
        return if (speed > -maxSpeed) {
            copy(speed = speed - acceleration)
        } else {
            this
        }
    }

    fun stop(): Starship = copy(speed = 0.0)

    fun rotate(degrees: Int): Starship = copy(rotation = rotation + degrees)
    override fun collide(collidable: Collidable): Starship {
        return when (collidable) {
            is Asteroid -> copy(life = life - collidable.getDamage())
            is Bullet -> if (collidable.getStarshipId() != id) copy(life = life - collidable.getDamage()) else this
            is Starship -> this
        }
    }

    override fun getLife(): Int = life

    override fun move(time: Double): Movable {
        val newPosition = mover.moveMovable(this, time)
        return copy(position = newPosition)
    }

    override fun getSpeed(): Double = this.speed;

    override fun getRotation(): Double = this.rotation

    override fun getPosition(): Position = this.position;
    override fun getId(): String = id;
    fun getWeapon(): Weapon = weapon
    fun getMover(): Mover = mover
    fun getName(): String = name
    fun getType(): Int = type
}
