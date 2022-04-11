package com.progix.fridgex.light.custom;

import android.graphics.Typeface;
import android.view.View;

import com.getkeepsafe.taptargetview.TapTarget;
import com.progix.fridgex.light.R;
import com.progix.fridgex.light.data.SharedPreferencesAccess;

import java.util.Objects;

public final class CustomTapTarget {

    public TapTarget create(
            View view, String title,
            String description, Integer nextId, Integer radius
    ) {
        int color;
        switch (Objects.requireNonNull(SharedPreferencesAccess.loadTheme(view.getContext()))) {
            case "red":
                color = R.color.dred2;
            case "pnk":
                color = R.color.dred;
            case "grn":
                color = R.color.dgreen;
            case "vlt":
                color = R.color.dviolet;
            case "yel":
                color = R.color.dyellow;
            case "mnt":
                color = R.color.dmint;
            case "ble":
                color = R.color.dblue;
            default:
                color = R.color.checked;
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
                .id(nextId);
    }

}