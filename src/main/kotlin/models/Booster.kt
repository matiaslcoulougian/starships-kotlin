package models;

data class Booster(
    private val id: String,
    private val type: BoosterType,
    private val position: Position,
    private val life: Int
): Collidable {
    fun getId(): String = id

    fun getType(): BoosterType = type

    fun getPosition(): Position = position

    override fun collide(collidable: Collidable): Collidable {
        return when (collidable){
            is Starship -> copy(life = 0)
            is Bullet -> copy(life = 0)
            else -> this
        }
    }

    override fun getLife(): Int = life;
}
