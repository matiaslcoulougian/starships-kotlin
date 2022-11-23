package models

import javafx.scene.input.KeyCode

data class GameState(
    val movables: List<Movable>,
    val keyBinding: Map<String, Map<KeyCode, Action>>,
    val status: GameStatus,
    val timeSinceLastAsteroid: Double,
    val scoreBoard: ScoreBoard,
    val gameConfig: GameConfig,
    val initialTime: Double
)