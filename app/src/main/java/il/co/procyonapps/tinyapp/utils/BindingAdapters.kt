package il.co.procyonapps.tinyapp.utils

import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.Lottie
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import il.co.procyonapps.tinyapp.R
import il.co.procyonapps.tinyapp.ui.MainViewModel

@BindingAdapter("setImageUrl")
fun ImageView.setImageUrl(url: String?) {
    Glide.with(this).load(url).fitCenter().into(this)
}

@BindingAdapter("setState")
fun LottieAnimationView.setState(state: MainViewModel.State?) {
    if (state == null) return
    repeatMode = LottieDrawable.RESTART
    if (state == MainViewModel.State.PLAY){
        pauseAnimation()
        repeatCount = 0
        alpha = 0.5f
    } else {
        repeatCount = LottieDrawable.INFINITE
        alpha = 1f
        playAnimation()
        
    }
    
}

@BindingAdapter("setIsGone")
fun View.isGone(isGone: Boolean?){
    if(isGone==null) return
    
    if (isGone){
        visibility = View.GONE
    } else{
        visibility = View.VISIBLE
    }
}

@BindingAdapter("setLoadingProgress")
fun View.loadProgress(progress: Int?){
    
    if (progress == null){
        foreground = null
    } else {
        
        if(foreground == null){
            val progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.plant_grow_progress, this.context.theme)
            foreground = progressDrawable
        }
        foreground.level = progress
    }
    
    
}