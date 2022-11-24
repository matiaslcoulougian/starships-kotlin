package utils


data class Configuration (
    val playersAmount: Int,
    val playersNames: List<String>,
    val starshipsLife: Int,
    val winningPoints: Int,
    val gameTime: Double,
    val keyBinding: List<KeyBindingConfig>,
    val starships: List<StarshipConfig>,
    val bullets: List<BulletConfig>,
    val asteroidSpawnRate: Double,
    val boosterSpawnRate: Double,
    val asteroidsDamage: Int
)