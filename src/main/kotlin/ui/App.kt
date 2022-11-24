package ui

import adapters.GameModelToUIAdapter
import edu.austral.ingsis.starships.ui.*
import factories.ClassicGameStateFactory
import javafx.application.Application
import javafx.application.Application.launch
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.stage.Stage
import models.GameStatus
import models.Starship
import persistance.loadGame
import persistance.saveGame

import services.GameEngine


fun main() {
    launch(Starships::class.java)
}

const val STAGE_HEIGHT = 800.0
const val STAGE_WIDTH = 800.0

class Starships() : Application() {
    private val imageResolver = CachedImageResolver(DefaultImageResolver())
    private val facade = ElementsViewFacade(imageResolver)
    private val keyTracker = KeyTracker()
    private val gameStateFactory = ClassicGameStateFactory()
    private val keysPressed: MutableList<KeyCode> = mutableListOf<KeyCode>()
    override fun start(primaryStage: Stage) {
        var gameEngine = GameEngine(gameStateFactory.buildGame(), gameStateFactory)
        val adapter = GameModelToUIAdapter()

        var starshipNames = gameEngine.gameState.movables.filterIsInstance<Starship>().map { Pair(it.getId(), it.getName()) }

        adapter.addElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)

        val pointsText = Text(getScoreText(gameEngine, starshipNames))
        val lifeText = Text(getLifeText(gameEngine))

        val stats = StackPane()

        pointsText.font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 20.0)
        pointsText.fill = Color.BLUE
        pointsText.stroke = Color.BLACK
        pointsText.strokeWidth = 1.0

        val pointsDiv = HBox()
        pointsDiv.alignment = Pos.TOP_LEFT
        pointsDiv.children.addAll(pointsText)
        pointsDiv.padding = Insets(10.0, 10.0, 10.0, 10.0)

        lifeText.font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 20.0)
        lifeText.fill = Color.BLUE
        lifeText.stroke = Color.BLACK
        lifeText.strokeWidth = 1.0

        val lifeDiv = HBox()
        lifeDiv.alignment = Pos.TOP_RIGHT
        lifeDiv.children.addAll(lifeText)
        lifeDiv.padding = Insets(10.0, 10.0, 10.0, 10.0)


        val timeText = Text("0")
        timeText.font = Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 30.0)
        timeText.fill = Color.BLUE
        timeText.stroke = Color.BLACK
        timeText.strokeWidth = 1.0 // setting stroke width to 2
        val timeDiv = HBox()
        timeDiv.alignment = Pos.TOP_CENTER
        timeDiv.children.addAll(timeText)
        timeDiv.padding = Insets(10.0, 10.0, 10.0, 10.0)


        val gameOverText = Text("")
        gameOverText.font = Font.font("Arial", FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 35.0)
        gameOverText.fill = Color.DARKBLUE
        gameOverText.stroke = Color.BLACK
        gameOverText.strokeWidth = 1.0 // setting stroke width to 2

        val gameOverDiv = HBox()
        gameOverDiv.alignment = Pos.CENTER
        gameOverDiv.children.addAll(gameOverText)
        gameOverDiv.padding = Insets(10.0, 10.0, 10.0, 10.0)

        stats.children.addAll(pointsDiv, lifeDiv, timeDiv)

        val pane = StackPane()

        facade.timeListenable.addEventListener(object: EventListener<TimePassed> {
            override fun handle(event: TimePassed) {
                if (gameEngine.gameState.status === GameStatus.PLAY) {
                    gameEngine = gameEngine.handleTimePassed(event.currentTimeInSeconds, event.secondsSinceLastTime)
                    adapter.updateElementsPosition(facade.elements, gameEngine.gameState.movables)
                    adapter.addElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
                    timeText.text = getTimeText(gameEngine, event.currentTimeInSeconds)
                    keysPressed.forEach {
                        gameEngine = gameEngine.handleKeyPressed(it, event.secondsSinceLastTime)
                        adapter.updateElementsPosition(facade.elements, gameEngine.gameState.movables)
                        adapter.addElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
                    }
                }
                if (gameEngine.gameState.status === GameStatus.OVER) {
                    if (!stats.children.contains(gameOverDiv)) {
                        stats.children.removeAll(pointsDiv, lifeDiv, timeDiv)
                        adapter.removeAllElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
                        gameOverText.text = getWinnerText(gameEngine)
                        stats.children.add(gameOverDiv)
                    }
                }
            }
        })

        facade.collisionsListenable.addEventListener(object: EventListener<Collision> {
            override fun handle(event: Collision) {
                if (gameEngine.gameState.status == GameStatus.PLAY) {
                    gameEngine = gameEngine.handleCollision(event.element1Id, event.element2Id)
                    adapter.removeElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
                    pointsText.text = getScoreText(gameEngine, starshipNames)
                    lifeText.text = getLifeText(gameEngine)
                }
            }
        })

        keyTracker.keyPressedListenable.addEventListener(object: EventListener<KeyPressed> {
            override fun handle(event: KeyPressed) {
                if (gameEngine.gameState.status === GameStatus.PLAY) {
                    if (!keysPressed.contains(event.key)) keysPressed.add(event.key)
                }
            }
        })

        facade.outOfBoundsListenable.addEventListener(object: EventListener<OutOfBounds> {
            override fun handle(event: OutOfBounds) {
                gameEngine = gameEngine.handleElementOutOfBounds(event.id)
                adapter.removeElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
            }
        })

        facade.reachBoundsListenable.addEventListener(object : EventListener<ReachBounds> {
            override fun handle(event: ReachBounds) {
                gameEngine = gameEngine.handleReachedBounds(event.id)
                adapter.updateElementsPosition(facade.elements, gameEngine.gameState.movables)
            }
        })

        keyTracker.keyReleasedListenable.addEventListener(object: EventListener<KeyReleased> {
            override fun handle(event: KeyReleased) {
                if (event.key === KeyCode.P) gameEngine = gameEngine.handlePauseAndResume()
                if (event.key === KeyCode.R && gameEngine.gameState.status == GameStatus.OVER) {
                    gameEngine = GameEngine(gameStateFactory.buildGame(), gameStateFactory)
                    starshipNames = gameEngine.gameState.movables.filterIsInstance<Starship>().map { Pair(it.getId(), it.getName()) }
                    if (stats.children.contains(gameOverDiv)) {
                        stats.children.addAll(pointsDiv, lifeDiv, timeDiv)
                        stats.children.remove(gameOverDiv)
                        pointsText.text = getScoreText(gameEngine, starshipNames)
                        lifeText.text = getLifeText(gameEngine)
                        adapter.addElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
                        return
                    }
                }
                if (event.key === KeyCode.S && gameEngine.gameState.status == GameStatus.PAUSE) saveGame(gameEngine)
                if (event.key === KeyCode.L && gameEngine.gameState.status == GameStatus.PAUSE) {
                    val loadedGame = loadGame()
                    if (loadedGame != null) {
                        gameEngine = loadedGame
                        starshipNames = gameEngine.gameState.movables.filterIsInstance<Starship>().map { Pair(it.getId(), it.getName()) }
                        adapter.addElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
                        pointsText.text = getScoreText(gameEngine, starshipNames)
                        lifeText.text = getLifeText(gameEngine)
                        return
                    }
                }
                gameEngine = gameEngine.handleShoot(event.key)
                adapter.updateElementsPosition(facade.elements, gameEngine.gameState.movables)
                adapter.addElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
                keysPressed.remove(event.key)
            }
        })

        facade.showGrid.set(false)
        facade.showCollider.set(false)

        val root = facade.view
        root.id = "pane"

        pane.children.addAll(root, stats)

        val scene = Scene(pane)
        keyTracker.scene = scene
        scene.stylesheets.add(this::class.java.classLoader.getResource("styles.css")?.toString())

        primaryStage.scene = scene
        primaryStage.height = STAGE_HEIGHT
        primaryStage.width = STAGE_WIDTH

        facade.start()
        keyTracker.start()
        primaryStage.show()
    }

    override fun stop() {
        facade.stop()
        keyTracker.stop()
    }

    private fun getScoreText(gameEngine: GameEngine, starshipNames: List<Pair<String, String>>): String {
        var scoreText = "Scores  "
        gameEngine.gameState.scoreBoard.getScore().forEach {
            scoreText += "  "
            val (key, value) = it
            val starship = starshipNames.find { it.first === key }
            if (starship != null) scoreText = scoreText + " " + starship.second + " " + value
        }
        return scoreText
    }

    private fun getLifeText(gameEngine: GameEngine): String {
        var lifeText = "Lives  "
        gameEngine.gameState.movables.filterIsInstance<Starship>()
            .sortedBy { it.getName() }
            .forEach { lifeText += "  " + it.getName() + " " + (it.getLife() / 5)}
        return lifeText
    }

    private fun getWinnerText(gameEngine: GameEngine): String {
        val winner = gameEngine.getWinner()
        return if (winner != null) {
            winner.getName() + " won!"
        }
        else {
            "Game Over"
        }
    }

    private fun getTimeText(gameEngine: GameEngine, currentTime: Double): String {
        val secondsLeft = (gameEngine.gameState.gameConfig.gameTime - (currentTime - gameEngine.gameState.initialTime)).toInt()
        return if (secondsLeft < 60 )
            if (secondsLeft < 10) "0$secondsLeft"
            else secondsLeft.toString()
        else {
            val minutes = (secondsLeft / 60).toInt()
            var stringMinutes = minutes.toString()
            if (minutes < 10) stringMinutes = "0$stringMinutes"
            val seconds = secondsLeft - minutes * 60
            val stringSeconds = seconds.toString()
            if (seconds < 10) "$stringMinutes:0$stringSeconds"
            else "$stringMinutes:$stringSeconds"
        }
    }

}