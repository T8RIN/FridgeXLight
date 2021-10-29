package com.progix.fridgex.light.custom

import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.Gravity.TOP
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.progix.fridgex.light.R

class CustomSnackbar(val context: Context) {
    fun create(
        view: View?,
        text: String?,
        duration: Int
    ): Snackbar {
        val snackBar = Snackbar.make(view!!, text!!, duration)
            .setActionTextColor(ContextCompat.getColor(context, R.color.checked))
        //.setBackgroundTint(ContextCompat.getColor(context, R.color.manualBackground))
        //.setTextColor(ContextCompat.getColor(context, R.color.manualText))
        val params = snackBar.view.layoutParams as CoordinatorLayout.LayoutParams
        params.anchorId = R.id.bottom_navigation
        params.anchorGravity = TOP
        params.gravity = TOP
        snackBar.view.layoutParams = params

        snackBar.view.translationY =
            -(5 * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT))

        return snackBar
    }

    fun create(
        leftMargin: Int,
        view: View?,
        text: String?,
        duration: Int
    ): Snackbar {
        val snackBar = Snackbar.make(view!!, text!!, duration)
            .setActionTextColor(ContextCompat.getColor(context, R.color.checked))
        //.setBackgroundTint(ContextCompat.getColor(context, R.color.manualBackground))
        //.setTextColor(ContextCompat.getColor(context, R.color.manualText))
        val params = snackBar.view.layoutParams as CoordinatorLayout.LayoutParams
        params.anchorId = R.id.tabs
        params.anchorGravity = TOP
        params.gravity = TOP
        snackBar.view.layoutParams = params

        snackBar.view.translationY =
            -(5 * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT))

        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val displayMetrics = context.resources.displayMetrics
            val dpWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt() - leftMargin
            val pixels = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpWidth.toFloat(),
                context.resources.displayMetrics
            )
            params.width = pixels.toInt()
            params.anchorId = R.id.tabs
            params.anchorGravity = Gravity.END
            params.gravity = Gravity.END
        }

        return snackBar
    }

    fun create(
        bottomMargin: Int,
        view: View?,
        text: String?
    ): Snackbar {
        val snackBar = Snackbar.make(view!!, text!!, Snackbar.LENGTH_SHORT)
            .setActionTextColor(ContextCompat.getColor(context, R.color.checked))
        //.setBackgroundTint(ContextCompat.getColor(context, R.color.manualBackground))
        //.setTextColor(ContextCompat.getColor(context, R.color.manualText))
        val params = snackBar.view.layoutParams as CoordinatorLayout.LayoutParams
        params.anchorId = R.id.bottom_navigation
        params.anchorGravity = TOP
        params.gravity = TOP
        snackBar.view.layoutParams = params

        snackBar.view.translationY =
            -(50 * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT))

        return snackBar
    }

    fun create(
        view: View?,
        text: String?,
        anchorGravity: Int,
        snackGravity: Int,
        anchorId: Int,
        duration: Int,
        translationY: Int
    ): Snackbar {
        val snackBar = Snackbar.make(view!!, text!!, duration)
            .setActionTextColor(ContextCompat.getColor(context, R.color.checked))
        //.setBackgroundTint(ContextCompat.getColor(context, R.color.manualBackground))
        //.setTextColor(ContextCompat.getColor(context, R.color.manualText))
        val params = snackBar.view.layoutParams as CoordinatorLayout.LayoutParams
        params.anchorId = anchorId
        params.anchorGravity = anchorGravity
        params.gravity = snackGravity
        snackBar.view.layoutParams = params

        snackBar.view.translationY =
            -(translationY * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT))

        return snackBar
    }


}