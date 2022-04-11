package com.progix.fridgex.light.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.progix.fridgex.light.R;

public class DialogLoadingFragment extends DialogFragment {

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.WRAP_CONTENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MaterialFadeThrough t = new MaterialFadeThrough();
        t.setDuration(getResources().getInteger(R.integer.anim_duration));
        setEnterTransition(t);
        setExitTransition(t);
    }

    public DialogLoadingFragment() {
        super(R.layout.fragment_dialog_loading);
    }

}