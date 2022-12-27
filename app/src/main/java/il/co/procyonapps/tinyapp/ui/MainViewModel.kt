package il.co.procyonapps.tinyapp.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import il.co.procyonapps.tinyapp.models.GamesItems
import il.co.procyonapps.tinyapp.models.GamesResponse

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val TAG = this::class.simpleName ?: "Unspecified"
    
    enum class State { PLAY, ORDER }
    
    val state = MutableLiveData(State.PLAY)
    private var _inMemoryGames: MutableList<GamesItems>? = null
    private val moshi = Moshi.Builder().build()
    private fun getGames(): MutableList<GamesItems> {
        return if (_inMemoryGames != null) {
            _inMemoryGames!!
        } else {
            val rawJson = getApplication<MyApp>().assets.open("collection.json").let {
                it.bufferedReader(Charsets.UTF_8).use { it.readText() }
            }
            val type = Types.newParameterizedType(List::class.java, GamesResponse.GamesResponseItem::class.java)
            val parsedResponse = moshi.adapter<List<GamesResponse.GamesResponseItem>>(type).fromJson(rawJson)
            val mappedItems = parsedResponse!!.map {
                GamesItems.Game(it.title, it.author, it.image) as GamesItems
            }.toMutableList()
//            val groupGames: List<GamesItems.Game> = mappedItems.take(8) as List<GamesItems.Game>
            val group = GamesItems.GameGroup("group A", mutableListOf())
            
            mappedItems.add(0, group)
            _inMemoryGames = mappedItems
            mappedItems
        }
    }
    
    val games: MutableLiveData<List<GamesItems>> = MutableLiveData(getGames())
    fun deleteGame(gameId: String) {
        val mutableList = games.value?.toMutableList() ?: return
        val success = mutableList?.removeIf { it.gameId == gameId }
        Log.d(TAG, "deleteGame: id=$gameId, removed? $success")
        games.postValue(mutableList)
    }
    
    fun mergeGame(sourceId: String, destinationId: String) {
        val gamesList = games.value?.toMutableList() ?: return
        val destination = gamesList.firstOrNull { it.gameId == destinationId }
        val sourceGame: GamesItems.Game = gamesList.firstOrNull { it.gameId == sourceId && it is GamesItems.Game } as GamesItems.Game
        if (sourceGame == null || destination == null) return
        val indexOfDest = gamesList.indexOf(destination)
        when (destination) {
            
            is GamesItems.Game -> {
                val currentGroupCount: Int = gamesList.filterIsInstance<GamesItems.GameGroup>().count()
                val newGroup = GamesItems.GameGroup("Group ${currentGroupCount + 1}", mutableListOf(sourceGame, destination))
                
                gamesList[indexOfDest] = newGroup
            }
            is GamesItems.GameGroup -> {
                
                val newGroupGamesList = listOf(sourceGame, *destination.games.toTypedArray())
                Log.d(TAG, "mergeGame: newList = ${newGroupGamesList.map { it.name }}")
                
                val newDest = destination.copy(games = newGroupGamesList)
                
                gamesList[indexOfDest] = newDest
            }
        }
        
        gamesList.remove(sourceGame)
        
        games.postValue(gamesList)
    }
}
