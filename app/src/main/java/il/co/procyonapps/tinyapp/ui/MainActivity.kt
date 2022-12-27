package il.co.procyonapps.tinyapp.ui

import android.content.ClipData
import android.content.ClipDescription
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View
import androidx.activity.viewModels
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionLayout.TransitionListener
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.ContentInfoCompat
import androidx.core.view.OnReceiveContentListener
import androidx.draganddrop.DropHelper
import androidx.recyclerview.widget.GridLayoutManager
import il.co.procyonapps.tinyapp.R
import il.co.procyonapps.tinyapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

     private var binder: ActivityMainBinding? = null
    val TAG = this::class.simpleName ?: "Unspecified"
    private val mainViewModel : MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binder!!.root)
        
        binder!!.apply{
            lifecycleOwner = this@MainActivity
            viewModel = mainViewModel
        }
        
        val gameAdapter = GameListAdapter(mainViewModel.state, this)
        binder!!.rvGamesList.apply{
            (layoutManager as GridLayoutManager).apply {
                spanCount = 5
            }
            adapter = gameAdapter
        }
        
        binder!!.mlStateSelector.addTransitionListener(object: TransitionListener{
            override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
            }
            override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {
            
            }
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                val switchState = resources.getResourceEntryName(currentId)
                Log.d(TAG, "onTransitionCompleted: id= $switchState")
                when(switchState){
                    "play" -> mainViewModel.state.postValue(MainViewModel.State.PLAY)
                    "order" -> mainViewModel.state.postValue((MainViewModel.State.ORDER))
                }
            }
            override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {
            }
        })
        
        
        val dropOptions = DropHelper.Options.Builder()
            .setHighlightColor(getColor(R.color.purpleVariant))
            .setHighlightCornerRadiusPx(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt())
            .build()
        DropHelper.configureView(this,
            binder!!.ltTrash,
            arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
            dropOptions
        ) { view, payload ->
            
            val(myData, everythingElse) = payload.partition { item: ClipData.Item -> item.text != null }
            
            val id = myData.clip.getItemAt(0).text
            mainViewModel.deleteGame(id.toString())
            everythingElse
        }
    
        mainViewModel.games.observe(this){
            Log.d(TAG, "observe game list: size ${it.size}")
            gameAdapter.submitList(it)
        }
    }
    
    override fun onDestroy() {
        binder = null
        super.onDestroy()
    }
}