package com.progix.fridgex.light.custom

import android.graphics.Typeface
import android.view.View
import com.getkeepsafe.taptargetview.TapTarget
import com.progix.fridgex.light.R
import com.progix.fridgex.light.data.SharedPreferencesAccess

class CustomTapTarget {

    fun create(
        view: View, title: String,
        description: String, nextId: Int, radius: Int
    ): TapTarget? {
        val color = when (SharedPreferencesAccess.loadTheme(view.context)) {
            "red" -> R.color.dred2
            "pnk" -> R.color.dred
            "grn" -> R.color.dgreen
            "vlt" -> R.color.dviolet
            "yel" -> R.color.dyellow
            "mnt" -> R.color.dmint
            "ble" -> R.color.dblue
            else -> R.color.checked
        }

        return TapTarget.forView(
            view,
            title,
            description
        )
            .outerCircleColor(color)
            .outerCircleAlpha(0.8f)
            .targetCircleColor(R.color.manualTarget)
            .titleTextSize(30)
            .titleTextColor(R.color.manualTitleTarget)
            .descriptionTextSize(20)
            .descriptionTextColor(R.color.manualDescriptionTarget)
            .textTypeface(Typeface.SANS_SERIF)
            .dimColor(R.color.black)
            .drawShadow(true)
            .cancelable(false)
            .tintTarget(true)
            .transparentTarget(false)
            .targetRadius(radius)
            .id(nextId)
    }

}