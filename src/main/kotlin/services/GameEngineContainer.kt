package services

data class GameEngineContainer(
    private var gameEngine: GameEngine
) {
    fun setGameEngine(gameEngine: GameEngine){ this.gameEngine = gameEngine }
    fun gameEngine(): GameEngine = gameEngine
}