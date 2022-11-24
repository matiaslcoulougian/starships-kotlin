package persistance

import com.google.gson.Gson
import models.GameState
import utils.Configuration
import java.io.File

fun saveGame(game: GameState) {
    val gson = Gson()
    File("./src/main/resources/game.txt").writeText(gson.toJson(game))
}

fun loadGame(): GameState {
    val gson = Gson()
    val gameJson = File("./src/main/resources/game.txt").readText(Charsets.UTF_8)
    return gson.fromJson(gameJson, GameState::class.java)
}

fun loadConfigs(): Configuration {
    val jsonString: String = File("./src/main/resources/configs/config.json").readText(Charsets.UTF_8)
    return Gson().fromJson(jsonString, Configuration::class.java)
}