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
    private var gameEngine = GameEngine(gameStateFactory.buildGame(), gameStateFactory)
    private val adapter = GameModelToUIAdapter()
    private val keysPressed: MutableList<KeyCode> = mutableListOf<KeyCode>()
    private var pausedTimePassed = 0.0
    private var starshipNames = getStarshipNames(gameEngine)
    override fun start(primaryStage: Stage) {
        adapter.addElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
        val (stats, texts, divs) = createGameStats(gameEngine, starshipNames)
        val (gameOverDiv, gameOverText) = createVisualElement("", Pos.CENTER, 35.0)

        timePassedListener(gameOverDiv, gameOverText, stats, divs, texts)
        collisionsListener(texts)
        keyPressedListener()
        outOfBoundsListener()
        reachBoundsListener()
        keyReleasedListener(texts, stats, gameOverDiv, divs)

        facade.showGrid.set(false)
        facade.showCollider.set(false)

        val scene = setScene(stats)
        keyTracker.scene = scene
        scene.stylesheets.add(this::class.java.classLoader.getResource("styles.css")?.toString())
        primaryStage.scene = scene
        primaryStage.height = STAGE_HEIGHT
        primaryStage.width = STAGE_WIDTH
        facade.start()
        keyTracker.start()
        primaryStage.show()
    }

    private fun setScene(stats: StackPane): Scene {
        val root = facade.view
        root.id = "pane"

        val pane = StackPane()

        pane.children.addAll(root, stats)

        return Scene(pane)
    }

    override fun stop() {
        facade.stop()
        keyTracker.stop()
    }
    private fun timePassedListener(gameOverDiv: HBox, gameOverText: Text, stats: StackPane, divs: List<HBox>, texts: Map<String, Text>) {
        facade.timeListenable.addEventListener(object: EventListener<TimePassed> {
            override fun handle(event: TimePassed) {
                when (gameEngine.gameState.status) {
                    GameStatus.PLAY -> {
                        handlePlayTimePassed(event, texts)
                    }
                    GameStatus.OVER -> {
                        handleOverTimePassed(stats, gameOverDiv, divs, gameOverText)
                    }
                    GameStatus.PAUSE -> pausedTimePassed += event.secondsSinceLastTime
                }
            }
        })
    }
    private fun handleOverTimePassed(stats: StackPane, gameOverDiv: HBox, divs: List<HBox>, gameOverText: Text) {
        if (!stats.children.contains(gameOverDiv)) {
            stats.children.removeAll(divs)
            adapter.removeAllElements(
                facade.elements,
                gameEngine.gameState.movables,
                gameEngine.gameState.boosters
            )
            gameOverText.text = getWinnerText(gameEngine)
            stats.children.add(gameOverDiv)
        }
    }

    private fun handlePlayTimePassed(event: TimePassed, texts: Map<String, Text>) {
        gameEngine = gameEngine.handleTimePassed(event.currentTimeInSeconds, event.secondsSinceLastTime)
        adapter.updateElementsPosition(facade.elements, gameEngine.gameState.movables)
        adapter.addElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
        texts["time"]?.text = getTimeText(gameEngine, event.currentTimeInSeconds)
        keysPressed.forEach {
            gameEngine = gameEngine.handleKeyPressed(it, event.secondsSinceLastTime)
            adapter.updateElementsPosition(facade.elements, gameEngine.gameState.movables)
            adapter.addElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
        }
    }

    private fun collisionsListener(texts: Map<String, Text>){
        facade.collisionsListenable.addEventListener(object: EventListener<Collision> {
            override fun handle(event: Collision) {
                if (gameEngine.gameState.status == GameStatus.PLAY) {
                    gameEngine = gameEngine.handleCollision(event.element1Id, event.element2Id)
                    adapter.removeElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
                    texts["score"]?.text = getScoreText(gameEngine, starshipNames)
                    texts["life"]?.text = getLifeText(gameEngine)
                }
            }
        })
    }

    private fun keyPressedListener(){
        keyTracker.keyPressedListenable.addEventListener(object: EventListener<KeyPressed> {
            override fun handle(event: KeyPressed) {
                if (gameEngine.gameState.status === GameStatus.PLAY) {
                    if (!keysPressed.contains(event.key)) keysPressed.add(event.key)
                }
            }
        })
    }

    private fun outOfBoundsListener() {
        facade.outOfBoundsListenable.addEventListener(object: EventListener<OutOfBounds> {
            override fun handle(event: OutOfBounds) {
                gameEngine = gameEngine.handleElementOutOfBounds(event.id)
                adapter.removeElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
            }
        })
    }

    private fun reachBoundsListener(){
        facade.reachBoundsListenable.addEventListener(object : EventListener<ReachBounds> {
            override fun handle(event: ReachBounds) {
                gameEngine = gameEngine.handleReachedBounds(event.id)
                adapter.updateElementsPosition(facade.elements, gameEngine.gameState.movables)
            }
        })
    }

    private fun keyReleasedListener(texts: Map<String, Text>, stats: StackPane, gameOverDiv: HBox, divs: List<HBox>){
        keyTracker.keyReleasedListenable.addEventListener(object: EventListener<KeyReleased> {
            override fun handle(event: KeyReleased) {
                if (event.key === KeyCode.P) handlePauseKey()
                else if (gameEngine.gameState.status == GameStatus.OVER || gameEngine.gameState.status == GameStatus.PAUSE) {
                    when (event.key) {
                        KeyCode.S -> saveGame(gameEngine.gameState)
                        KeyCode.L -> loadGame(texts)
                        KeyCode.R -> handleResetGame(stats, texts, gameOverDiv, divs)
                        else -> {}
                    }
                }
                gameEngine = gameEngine.handleShoot(event.key)
                adapter.updateElementsPosition(facade.elements, gameEngine.gameState.movables)
                adapter.addElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
                keysPressed.remove(event.key)
            }
        })
    }

    private fun loadGame(texts: Map<String, Text>) {
        val loadedGame = loadGame()
        gameEngine = gameEngine.copy(gameState = loadedGame)
        starshipNames = getStarshipNames(gameEngine)
        adapter.addElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
        texts["score"]?.text = getScoreText(gameEngine, starshipNames)
        texts["life"]?.text = getLifeText(gameEngine)
    }

    private fun handleResetGame(stats: StackPane, texts: Map<String, Text>, gameOverDiv: HBox, divs: List<HBox>) {
        adapter.removeAllElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
        gameEngine = GameEngine(gameStateFactory.buildGame(), gameStateFactory)
        starshipNames = getStarshipNames(gameEngine)
        adapter.addElements(facade.elements, gameEngine.gameState.movables, gameEngine.gameState.boosters)
        if (stats.children.contains(gameOverDiv)) resetVisualComponents(stats, divs, gameOverDiv, texts)
    }

    private fun resetVisualComponents(
        stats: StackPane,
        divs: List<HBox>,
        gameOverDiv: HBox,
        texts: Map<String, Text>
    ) {
        stats.children.addAll(divs)
        stats.children.remove(gameOverDiv)
        texts["points"]?.text = getScoreText(gameEngine, starshipNames)
        texts["life"]?.text = getLifeText(gameEngine)
    }

    private fun handlePauseKey() {
        if (gameEngine.gameState.status == GameStatus.PLAY) pausedTimePassed = 0.0
        gameEngine = gameEngine.handlePauseAndResume(pausedTimePassed)
    }


    private fun createVisualElement(
        initialText: String,
        alignment: Pos,
        size: Double
    ): Pair<HBox, Text> {
        val text = Text(initialText)

        text.font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, size)
        text.fill = Color.BLUE
        text.stroke = Color.BLACK
        text.strokeWidth = 1.0

        val div = HBox()
        div.alignment = alignment
        div.children.addAll(text)
        div.padding = Insets(10.0, 10.0, 10.0, 10.0)
        return Pair(div, text)
    }

    private fun createGameStats(
        gameEngine: GameEngine,
        starshipNames: List<Pair<String, String>>
    ): Triple<StackPane, Map<String, Text>, List<HBox>> {
        val stats = StackPane()
        val (scoreDiv, scoreText) = createVisualElement(getScoreText(gameEngine, starshipNames), Pos.TOP_LEFT, 20.0)
        val (lifeDiv, lifeText) = createVisualElement(getLifeText(gameEngine), Pos.TOP_RIGHT, 20.0)
        val (timeDiv, timeText) = createVisualElement("0", Pos.TOP_CENTER, 20.0)
        stats.children.addAll(scoreDiv, lifeDiv, timeDiv)
        val texts = mapOf(Pair("time", timeText), Pair("life", lifeText), Pair("score", scoreText))
        return Triple(stats, texts, listOf(scoreDiv, lifeDiv, timeDiv))
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

    private fun getStarshipNames(gameEngine: GameEngine) = gameEngine.gameState.movables.filterIsInstance<Starship>().map { Pair(it.getId(), it.getName()) }

}