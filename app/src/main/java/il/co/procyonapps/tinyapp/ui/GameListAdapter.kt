package il.co.procyonapps.tinyapp.ui

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Build
import android.provider.DocumentsContract.Root
import android.util.Log
import android.view.ContentInfo
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnDragListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout.generateViewId
import androidx.core.content.ContextCompat
import androidx.core.view.ContentInfoCompat
import androidx.core.view.OnReceiveContentListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import il.co.procyonapps.tinyapp.R
import il.co.procyonapps.tinyapp.databinding.GameItemBinding
import il.co.procyonapps.tinyapp.databinding.GroupItemBinding
import il.co.procyonapps.tinyapp.models.GamesItems
import il.co.procyonapps.tinyapp.utils.dpToPx
import il.co.procyonapps.tinyapp.utils.pxToDp
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
                oldItem == newItem && oldItem.games.size == newItem.games.size
            } else {
                false
            }
        }
    }
    
    var mergeGames: ((dropped: String, target: String ) -> Unit)? = null
    
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
                setupGameCell(holder, item)
            }
            is ItemViewHolder.GroupViewHolder -> {
                setupGroupCell(item, holder)
            }
        }
        
        holder.itemView.foreground = null
        
        holder.itemView.setOnDragListener{ v, event ->
    
            when(event.action){
                DragEvent.ACTION_DRAG_STARTED ->{
                    if(event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) && event.clipDescription.label != item.gameId){
                        v.foreground = ContextCompat.getDrawable(holder.itemView.context, R.drawable.drag_target_overlay)
                        true
                    } else {
                        false
                    }
                }
        
                DragEvent.ACTION_DRAG_ENTERED ->{
                    v.foreground = ContextCompat.getDrawable(holder.itemView.context, R.drawable.drag_target_overlay_marked)
                    true
                }
        
                DragEvent.ACTION_DRAG_EXITED -> {
                    v.foreground = ContextCompat.getDrawable(holder.itemView.context, R.drawable.drag_target_overlay)
                    true
                }
        
                DragEvent.ACTION_DROP ->{
                    val idOfDropped = event.clipData.getItemAt(0).text.toString()
                    val idOfTarget = item.gameId
            
                    mergeGames?.invoke(idOfDropped, idOfTarget)
            
                    true
                }
        
                DragEvent.ACTION_DRAG_ENDED ->{
                    v.foreground = null
                    true
                }
                else -> true
            }
        }
    }
    
    private fun setupGroupCell(item: GamesItems?, holder: ItemViewHolder.GroupViewHolder) {
        val group = item as GamesItems.GameGroup
        val referencedIds = holder.binder.flowGroup.referencedIds
        val constraintLayout = holder.binder.root as ConstraintLayout
        if(referencedIds.size != group.games.size) {
            
            //remove old views
            referencedIds.forEach {
                val view = constraintLayout.findViewById<View>(it)
                constraintLayout.removeView(view)
            }
            
            val ids: List<Int> = group.games.map { game ->
                val iv: ImageView = ImageView(holder.itemView.context).apply {
                    layoutParams = LayoutParams(30.dpToPx.toInt(), 30.dpToPx.toInt())
                    id = generateViewId()
                    Glide.with(this).load(game.imageUrl).centerCrop().into(this)
                }
                
                constraintLayout.addView(iv)
                iv.id
            }
            holder.binder.flowGroup.referencedIds = ids.toIntArray()
            holder.binder.invalidateAll()
        }
        
    }
    
    
    
    private fun setupGameCell(holder: ItemViewHolder.GameViewHolder, item: GamesItems) {
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
                if (this@GameListAdapter.state.value == MainViewModel.State.PLAY) {
                    //show loading
                    val prog = liveData<Int?>(lifecycle.lifecycleScope.coroutineContext, Duration.ofSeconds(10)) {
                        emit(0)
                        while (this.latestValue!! < 100) {
                            delay(60)
                            emit(latestValue!! + 1)
                        }
                        delay(3000)
                        emit(null)
                    }
                    progress = prog
                }
            }
        }
    }
}