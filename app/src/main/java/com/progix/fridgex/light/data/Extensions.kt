package com.progix.fridgex.light.data

import android.content.Context
import android.util.TypedValue

object Extensions {

    fun Context.dipToPixels(dipValue: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.displayMetrics)

}