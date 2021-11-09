package com.progix.fridgex.light.custom

import android.app.Activity
import android.graphics.Typeface
import android.view.View
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.progix.fridgex.light.R

class CustomTapTarget(
    val activity: Activity,
) {


    fun createAndShow(
        listener: TapTargetView.Listener, view: View, title: String,
        description: String
    ) {
        TapTargetView.showFor(
            activity,
            TapTarget.forView(
                view,
                title,
                description
            )
                .outerCircleColor(R.color.checked) // Specify a color for the outer circle
                .outerCircleAlpha(0.8f) // Specify the alpha amount for the outer circle
                .targetCircleColor(R.color.white) // Specify a color for the target circle
                //.titleTextSize(20) // Specify the size (in sp) of the title text
                //.titleTextColor(R.color.white) // Specify the color of the title text
                //.descriptionTextSize(10) // Specify the size (in sp) of the description text
                //.descriptionTextColor(R.color.red) // Specify the color of the description text
                //.textColor(R.color.blue) // Specify a color for both the title and description text
                .textTypeface(Typeface.SANS_SERIF) // Specify a typeface for the text
                .dimColor(R.color.black) // If set, will dim behind the view with 30% opacity of the given color
                .drawShadow(true) // Whether to draw a drop shadow or not
                .cancelable(false) // Whether tapping outside the outer circle dismisses the view
                .tintTarget(true) // Whether to tint the target view's color
                .transparentTarget(false) // Specify whether the target is transparent (displays the content underneath)
                //.icon(Drawable) // Specify a custom drawable to draw as the target
                .targetRadius(50),  // Specify the target radius (in dp)
            listener
        )
    }

    fun create(
        view: View, title: String,
        description: String, nextId: Int, radius: Int
    ): TapTarget? {
        return TapTarget.forView(
            view,
            title,
            description
        )
            .outerCircleColor(R.color.checked) // Specify a color for the outer circle
            .outerCircleAlpha(0.8f) // Specify the alpha amount for the outer circle
            .targetCircleColor(R.color.manualTarget) // Specify a color for the target circle
            .titleTextSize(30) // Specify the size (in sp) of the title text
            .titleTextColor(R.color.manualTitleTarget) // Specify the color of the title text
            .descriptionTextSize(20) // Specify the size (in sp) of the description text
            .descriptionTextColor(R.color.manualDescriptionTarget) // Specify the color of the description text
            //.textColor(R.color.blue) // Specify a color for both the title and description text
            .textTypeface(Typeface.SANS_SERIF) // Specify a typeface for the text
            .dimColor(R.color.black) // If set, will dim behind the view with 30% opacity of the given color
            .drawShadow(true) // Whether to draw a drop shadow or not
            .cancelable(false) // Whether tapping outside the outer circle dismisses the view
            .tintTarget(true) // Whether to tint the target view's color
            .transparentTarget(false) // Specify whether the target is transparent (displays the content underneath)
            //.icon(Drawable) // Specify a custom drawable to draw as the target
            .targetRadius(radius)
            .id(nextId)
    }

}