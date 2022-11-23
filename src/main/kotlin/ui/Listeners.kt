package ui

import edu.austral.ingsis.starships.ui.*
import javafx.scene.text.Text
import models.GameStatus
import models.Starship
import services.GameEngine

//class TimeListener(private val elements: MutableMap<String, ElementModel>, var gameEngine: GameEngine) :
//    EventListener<TimePassed> {
//    override fun handle(event: TimePassed) {
//        if (gameEngine.gameState.status === GameStatus.PLAY) {
//            gameEngine = gameEngine.handleTimePassed(event.currentTimeInSeconds, event.secondsSinceLastTime)
//            gameEngine.updateElementsPosition(elements)
//            gameEngine.addElements(elements)
//        }
//    }
//}
//
//class CollisionListener(private val elements: MutableMap<String, ElementModel>, var gameEngine: GameEngine, private var pointsText: Text, private var lifeText: Text) :
//    EventListener<Collision> {
//    override fun handle(event: Collision) {
//        gameEngine = gameEngine.handleCollision(event.element1Id, event.element2Id)
//        gameEngine.removeElements(elements)
//        pointsText.text = getScoreText(gameEngine)
//        lifeText.text = getLifeText(gameEngine)
//    }
//
//    private fun getScoreText(gameEngine: GameEngine): String {
//        var scoreText = "Scores  "
//        gameEngine.gameState.scoreBoard.getScore().forEach {
//            scoreText += "  "
//            val (key, value) = it
//            val starship = gameEngine.gameState.movables.find { it.getId() === key } as Starship
//            scoreText = scoreText + " " + starship.getName() + " " + value
//        }
//        return scoreText
//    }
//
//    private fun getLifeText(gameEngine: GameEngine): String {
//        var lifeText = "Lives  "
//        gameEngine.gameState.movables.filterIsInstance<Starship>()
//            .sortedBy { it.getName() }
//            .forEach { lifeText += "  " + it.getName() + " " + it.getLife() }
//        return lifeText
//    }
//
//}
//
//class KeyPressedListener(private val elements: MutableMap<String, ElementModel>, var gameEngine: GameEngine):
//    EventListener<KeyPressed> {
//    override fun handle(event: KeyPressed) {
//        event.currentPressedKeys.forEach {
//            gameEngine = gameEngine.handleKeyPressed(it)
//            gameEngine.updateElementsPosition(elements)
//            gameEngine.addElements(elements)
//        }
//    }
//
//}
//
//class OutOfBoundsListener(private val elements: MutableMap<String, ElementModel>, var gameEngine: GameEngine) :
//    EventListener<OutOfBounds> {
//    override fun handle(event: OutOfBounds) {
//        gameEngine = gameEngine.handleElementOutOfBounds(event.id)
//        gameEngine.removeElements(elements)
//    }
//}
//
//class ReachBoundsListener(private val elements: MutableMap<String, ElementModel>, var gameEngine: GameEngine) :
//    EventListener<ReachBounds> {
//    override fun handle(event: ReachBounds) {
//        gameEngine = gameEngine.handleReachedBounds(event.id)
//        gameEngine.updateElementsPosition(elements)
//    }
//}