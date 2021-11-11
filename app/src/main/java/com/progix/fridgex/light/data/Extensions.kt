package com.progix.fridgex.light.data

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.util.TypedValue
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat

object Extensions {

    fun Context.dipToPixels(dipValue: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.displayMetrics)

    fun ImageView.setTint(@ColorRes colorRes: Int?) {
        if (colorRes != null) {
            ImageViewCompat.setImageTintMode(this, PorterDuff.Mode.SRC_ATOP)
            ImageViewCompat.setImageTintList(
                this,
                ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
            )
        } else ImageViewCompat.setImageTintList(this, null)
    }
}