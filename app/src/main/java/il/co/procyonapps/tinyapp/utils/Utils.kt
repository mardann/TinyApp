package il.co.procyonapps.tinyapp.utils

import android.content.Context
import android.content.res.Configuration
import android.telephony.TelephonyManager

fun Context.isTablet(): Boolean{
    return ((resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE)
}