package com.progix.fridgex.light.application;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.progix.fridgex.light.data.SharedPreferencesAccess;

import java.util.ArrayList;

public class FridgeXLightApplication extends Application {

    public FridgeXLightApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        switch (SharedPreferencesAccess.loadNightMode(this)) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        heightPixels = getResources().getDisplayMetrics().heightPixels;
        widthPixels = getResources().getDisplayMetrics().widthPixels;
    }

    public static Context appContext;
    public static ArrayList<String> allProducts = null;
    public static ArrayList<String> allHints = null;
    public static int heightPixels = 0;
    public static int widthPixels = 0;

    private Context mCurrentContext = null;

    public Context getCurrentContext() {
        return mCurrentContext;
    }

    public void setCurrentContext(Context mCurrentContext) {
        this.mCurrentContext = mCurrentContext;
    }

}