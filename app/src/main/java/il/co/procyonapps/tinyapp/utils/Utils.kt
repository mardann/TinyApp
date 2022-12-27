package il.co.procyonapps.tinyapp.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.telephony.TelephonyManager

fun Context.isTablet(): Boolean{
    return ((resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE)
}

val Number.pxToDp
    get() = this.toFloat() / Resources.getSystem().displayMetrics.density

val Number.dpToPx
    get() = this.toFloat() * Resources.getSystem().displayMetrics.density

val Number.spToPx
    get() = this.toFloat() * Resources.getSystem().displayMetrics.scaledDensity