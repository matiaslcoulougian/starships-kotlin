package persistance

import com.google.gson.Gson
import services.GameEngine
import java.io.File

fun saveGame(game: GameEngine) {
    val gson = Gson()
    File("./src/main/resources/game.txt").writeText(gson.toJson(game))
}

fun loadGame(): GameEngine? {
    val gson = Gson()
    val gameJson = File("./src/main/resources/game.txt").readText(Charsets.UTF_8)
    return Gson().fromJson(gameJson, GameEngine::class.java)
}