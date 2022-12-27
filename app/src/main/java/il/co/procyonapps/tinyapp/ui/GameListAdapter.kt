package il.co.procyonapps.tinyapp.ui

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.util.Log
import android.view.ContentInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.ContentInfoCompat
import androidx.core.view.OnReceiveContentListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import il.co.procyonapps.tinyapp.databinding.GameItemBinding
import il.co.procyonapps.tinyapp.databinding.GroupItemBinding
import il.co.procyonapps.tinyapp.models.GamesItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration

class GameListAdapter(val state: LiveData<MainViewModel.State>, val lifecycle: LifecycleOwner) : ListAdapter<GamesItems, GameListAdapter.ItemViewHolder>(GamesDiffer) {
    val TAG = this::class.simpleName ?: "Unspecified"
    
    companion object {
        const val GAME_TYPE = 0
        const val GROUP_TYPE = 1
    }
    
    object GamesDiffer : DiffUtil.ItemCallback<GamesItems>() {
        override fun areItemsTheSame(oldItem: GamesItems, newItem: GamesItems): Boolean {
            return oldItem.gameId == newItem.gameId
        }
        
        override fun areContentsTheSame(oldItem: GamesItems, newItem: GamesItems): Boolean {
            return if (oldItem is GamesItems.Game && newItem is GamesItems.Game) {
                oldItem == newItem
            } else if (oldItem is GamesItems.GameGroup && newItem is GamesItems.GameGroup) {
                oldItem == newItem
            } else {
                false
            }
        }
    }
    
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GamesItems.Game -> GAME_TYPE
            is GamesItems.GameGroup -> GROUP_TYPE
        }
    }
    
    sealed class ItemViewHolder(view: View) : ViewHolder(view) {
        class GameViewHolder(val binder: GameItemBinding) : ItemViewHolder(binder.root)
        class GroupViewHolder(val binder: GroupItemBinding) : ItemViewHolder(binder.root)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return when (viewType) {
            GAME_TYPE -> {
                val binder = GameItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ItemViewHolder.GameViewHolder(binder)
            }
            GROUP_TYPE -> {
                val binder = GroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ItemViewHolder.GroupViewHolder(binder)
            }
            else -> throw IllegalStateException("Non legal view type $viewType")
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ItemViewHolder.GameViewHolder -> {
                holder.binder.apply {
                    lifecycleOwner = lifecycle
                    game = item as GamesItems.Game
                    state = this@GameListAdapter.state
                    root.setOnLongClickListener {
                        if (this@GameListAdapter.state.value == MainViewModel.State.ORDER) {
                            val shadow = View.DragShadowBuilder(it)
                            val clipItem = ClipData.Item(item.gameId as CharSequence)
                            val dragData = ClipData(
                                item.gameId as CharSequence,
                                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                                clipItem
                            )
                            val dragResult = root.startDragAndDrop(dragData, shadow, null, 0)
                            Log.d(TAG, "onBindViewHolder: click - order. success? $dragResult")
                            true
                        } else {
                            false
                        }
                    }
                    
                    root.setOnClickListener {
                        if(this@GameListAdapter.state.value ==  MainViewModel.State.PLAY){
                            //show loading
                            
                            val prog = liveData<Int?>(lifecycle.lifecycleScope.coroutineContext, Duration.ofSeconds(10)){
                                emit(0)
                                while (this.latestValue!! < 100){
                                    delay(60)
                                    emit(latestValue!!+1)
                                }
                                delay(3000)
                                emit(null)
                            }
                            progress = prog
                            
                        }
                    }
                    
                }
            }
            is ItemViewHolder.GroupViewHolder -> {
                //todo - implement drawing
            }
        }
    }
}