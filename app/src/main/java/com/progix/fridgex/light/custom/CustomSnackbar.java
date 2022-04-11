package com.progix.fridgex.light.custom;

import static android.view.Gravity.TOP;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.progix.fridgex.light.R;

@SuppressLint("ShowToast")
public class CustomSnackbar {

    Context context;

    public CustomSnackbar(Context context) {
        this.context = context;
    }

    public Snackbar create(View view, String text, Integer duration) {
        Snackbar snackBar = Snackbar.make(view, text, duration)
                .setActionTextColor(getAttrColor(R.attr.checked, context));
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackBar.getView().getLayoutParams();
        params.setAnchorId(R.id.bottom_navigation);
        params.anchorGravity = TOP;
        params.gravity = TOP;
        snackBar.getView().setLayoutParams(params);

        snackBar.getView().setTranslationY(-(5 * (float) (context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT)));

        return snackBar;
    }

    public Snackbar create(Boolean indefinite, View view, String text) {
        int length = Snackbar.LENGTH_LONG;
        if (indefinite) {
            length = Snackbar.LENGTH_INDEFINITE;
        }
        Snackbar snackBar = Snackbar.make(view, text, length)
                .setActionTextColor(getAttrColor(R.attr.checked, context));
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackBar.getView().getLayoutParams();
        params.setAnchorId(R.id.bottom_navigation);
        params.anchorGravity = TOP;
        params.gravity = TOP;
        snackBar.getView().setLayoutParams(params);

        snackBar.getView().setTranslationY(-(5 * (float) (context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT)));

        return snackBar;
    }

    public Snackbar create(Integer leftMargin, View view, String text, Integer duration) {
        Snackbar snackBar = Snackbar.make(view, text, duration)
                .setActionTextColor(getAttrColor(R.attr.checked, context));
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackBar.getView().getLayoutParams();
        params.setAnchorId(R.id.tabs);
        params.anchorGravity = TOP;
        params.gravity = TOP;
        snackBar.getView().setLayoutParams(params);

        snackBar.getView().setTranslationY(-(5 * (float) (context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT)));

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int dpWidth = (int) (displayMetrics.widthPixels / displayMetrics.density) - leftMargin;
            float pixels = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    (float) dpWidth,
                    context.getResources().getDisplayMetrics()
            );
            params.width = (int) pixels;
            params.setAnchorId(R.id.tabs);
            params.anchorGravity = Gravity.END;
            params.gravity = Gravity.END;
        }

        return snackBar;
    }

    public Snackbar create(Integer bottomMargin, View view, String text) {
        Snackbar snackBar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
                .setActionTextColor(getAttrColor(R.attr.checked, context));
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackBar.getView().getLayoutParams();
        params.setAnchorId(R.id.bottom_navigation);
        params.anchorGravity = TOP;
        params.gravity = TOP;
        snackBar.getView().setLayoutParams(params);

        snackBar.getView().setTranslationY(-((bottomMargin - 5)) * (float) (context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT)))
        ;

        return snackBar;
    }

}