package il.co.procyonapps.tinyapp.models

import java.util.*

sealed class GamesItems(val gameId: String) {
    data class Game(val name: String, val author: String, val imageUrl: String): GamesItems("game_${UUID.randomUUID()}")
    data class GameGroup  constructor(val groupName: String, val games: List<Game>): GamesItems("group_${UUID.randomUUID()}"){
    
    }
}