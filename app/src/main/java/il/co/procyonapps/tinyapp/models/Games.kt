package il.co.procyonapps.tinyapp.models

import java.util.*

sealed class GamesItems(val gameId: String) {
    data class Game(val name: String, val author: String, val imageUrl: String): GamesItems("game_${UUID.randomUUID()}")
    data class GameGroup private constructor(val groupName: String, val games: MutableList<Game>): GamesItems("group_${UUID.randomUUID()}"){
        constructor(name: String, vararg initialGames: Game): this(name, mutableListOf(*initialGames))
        fun addGame(newGame: Game){
            games.add(newGame)
        }
        
        fun removeGame(gameToRemove: Game){
            games.remove(gameToRemove)
        }
    }
}