package models

import javafx.scene.input.KeyCode

data class GameConfig(
    val playersAmount: Int,
    val winningPoints: Int,
    val gameTime: Double,
    val keyBinding: Map<String, Map<KeyCode, Action>>,
    val asteroidSpawnRate: Double, //seconds
    val asteroidsDamage: Int,
    val boosterSpawnRate: Double //seconds
)