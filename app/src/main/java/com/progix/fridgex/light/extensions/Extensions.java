package com.progix.fridgex.light.extensions;

import android.content.Context;
import android.content.res.Configuration;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.progix.fridgex.light.R;
import com.progix.fridgex.light.activity.MainActivity;
import com.progix.fridgex.light.data.DataArrays;
import com.progix.fridgex.light.data.SharedPreferencesAccess;
import com.progix.fridgex.light.fragment.dialog.DialogLoadingFragment;
import com.progix.fridgex.light.helper.DatabaseHelper;

import java.util.Locale;
import java.util.regex.Pattern;


public final class Extensions {

    public static float dipToPixels(Float dipValue, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, context.getResources().getDisplayMetrics());
    }

    public static Integer getAttrColor(Integer attrColor, Context context) {
        TypedValue t = new TypedValue();
        context.getTheme().resolveAttribute(attrColor, t, true);
        return t.data;
    }

    public static Context adjustFontSize(Float fontScale, Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.fontScale = fontScale;
        return context.createConfigurationContext(configuration);
    }

    public static void initDataBase(AppCompatActivity activity) {
        if (DataArrays.languages.contains(Locale.getDefault().getDisplayLanguage())) {
            DatabaseHelper.DB_NAME = "FridgeXX.db";
        } else {
            DatabaseHelper.DB_NAME = "FridgeXX_en.db";
        }
        DatabaseHelper mDBHelper = new DatabaseHelper(activity);
        MainActivity.mDb = mDBHelper.getWritableDatabase();
        if (SharedPreferencesAccess.loadBoolean(activity, "triedOnce") && !SharedPreferencesAccess.loadBoolean(activity, "upgraded") || DatabaseHelper.mNeedUpdate) {
            SharedPreferencesAccess.saveBoolean(activity, "triedOnce", true);
            SharedPreferencesAccess.saveBoolean(activity, "upgraded", false);
            DatabaseHelper.mNeedUpdate = true;
            new MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.updatedRecently))
                    .setMessage(activity.getString(R.string.updateMessage))
                    .setPositiveButton(activity.getString(R.string.update), (v, b) -> {
                        DialogLoadingFragment loadingFragment = new DialogLoadingFragment();
                        loadingFragment.setCancelable(false);

                        if (!loadingFragment.isAdded()) loadingFragment.show(
                                activity.getSupportFragmentManager(),
                                "loading"
                        );
                        new Thread(mDBHelper::updateDataBase).start();
                        loadingFragment.dismiss();
                        SharedPreferencesAccess.saveBoolean(activity.getApplicationContext(), "upgraded", true);
                        Toast.makeText(
                                        activity.getApplicationContext(),
                                        activity.getString(R.string.bdSuccess),
                                        Toast.LENGTH_SHORT
                                )
                                .show();
                        MainActivity.mDb = mDBHelper.getWritableDatabase();
                    })
                    .setCancelable(false)
                    .show();
            SharedPreferencesAccess.saveBoolean(activity, "upgraded", false);
        }
    }


    public static Boolean isNumeric(String string) {
        if (string == null) {
            return false;
        } else {
            return Pattern.compile("-?\\d+(\\.\\d+)?").matcher(string).matches();
        }
    }
}