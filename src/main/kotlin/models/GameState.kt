package models

import javafx.scene.input.KeyCode

data class GameState(
    val movables: List<Movable>,
    val boosters: List<Booster>,
    val status: GameStatus,
    val timeSinceLastAsteroid: Double,
    val timeSinceLastBooster: Double,
    val scoreBoard: ScoreBoard,
    val gameConfig: GameConfig,
    val initialTime: Double
)