package main

import adapters.GameModelToUIAdapter
import edu.austral.ingsis.starships.ui.*
import factories.ClassicGameStateFactory
import javafx.application.Application
import javafx.application.Application.launch
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.stage.Stage
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
    private var gameEngine: GameEngine = GameEngine(gameStateFactory.buildGame(), GameModelToUIAdapter(), gameStateFactory)

    override fun start(primaryStage: Stage) {
        gameEngine.addElements(facade.elements)

        facade.timeListenable.addEventListener(object: EventListener<TimePassed> {
            override fun handle(event: TimePassed) {
                gameEngine = gameEngine.handleTimePassed(event.currentTimeInSeconds, event.secondsSinceLastTime)
                gameEngine.updateElementsPosition(facade.elements)
                gameEngine.addElements(facade.elements)
            }
        })

        facade.collisionsListenable.addEventListener(object: EventListener<Collision> {
            override fun handle(event: Collision) {
                gameEngine = gameEngine.handleCollision(event.element1Id, event.element2Id)
                gameEngine.removeElements(facade.elements)
            }
        })

        keyTracker.keyPressedListenable.addEventListener(object: EventListener<KeyPressed> {
            override fun handle(event: KeyPressed) {
                gameEngine = gameEngine.handleKeysPressed(event.currentPressedKeys)
                gameEngine.updateElementsPosition(facade.elements)
                gameEngine.addElements(facade.elements)
            }
        })

        facade.outOfBoundsListenable.addEventListener(object: EventListener<OutOfBounds> {
            override fun handle(event: OutOfBounds) {
                gameEngine = gameEngine.handleElementOutOfBounds(event.id)
                gameEngine.removeElements(facade.elements)
            }
        })

        facade.reachBoundsListenable.addEventListener(object : EventListener<ReachBounds> {
            override fun handle(event: ReachBounds) {
                gameEngine = gameEngine.handleReachedBounds(event.id)
                gameEngine.updateElementsPosition(facade.elements)
            }
        })

        facade.showGrid.set(false)
        facade.showCollider.set(false)

//        val score = StackPane()
//        val points = Text("Score: 0")
//        points.font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 20.0)
//        points.fill = Color.BLUE
//        points.stroke = Color.BLACK
//        points.strokeWidth = 1.0 // setting stroke width to 2
//        val div = HBox()
//        div.alignment = Pos.TOP_CENTER
//        div.children.addAll(points)
//        div.padding = Insets(10.0, 10.0, 10.0, 10.0)

        //score.children.addAll(div)

        //val pane = StackPane()

        val root = facade.view
        root.id = "pane"

        //pane.children.addAll(root, score)

        val scene = Scene(root)
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
}