package com.progix.fridgex.light.custom

import android.app.Activity
import android.graphics.Typeface
import android.view.View
import com.getkeepsafe.taptargetview.TapTarget
import com.progix.fridgex.light.R

class CustomTapTarget(
    val activity: Activity,
) {

    fun create(
        view: View, title: String,
        description: String, nextId: Int, radius: Int
    ): TapTarget? {
        return TapTarget.forView(
            view,
            title,
            description
        )
            .outerCircleColor(R.color.checked)
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