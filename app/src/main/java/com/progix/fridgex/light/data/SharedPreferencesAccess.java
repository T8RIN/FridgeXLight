package com.progix.fridgex.light.data;

import android.content.Context;
import android.content.SharedPreferences;

public final class SharedPreferencesAccess {

    public static void saveBoolean(Context context, String key, Boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static Boolean loadBoolean(Context context, String key) {
        return context.getSharedPreferences("fridgex", Context.MODE_PRIVATE).getBoolean(key, true);
    }

    public static void saveString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String loadString(Context context, String key) {
        return context.getSharedPreferences("fridgex", Context.MODE_PRIVATE).getString(key, "0");
    }

    public static Integer loadNightMode(Context context) {
        return context.getSharedPreferences("fridgex", Context.MODE_PRIVATE).getInt("mode", 2);
    }

    public static void saveNightMode(Context context, Integer value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("mode", value);
        editor.apply();
    }

    public static Integer loadCartMode(Context context) {
        return context.getSharedPreferences("fridgex", Context.MODE_PRIVATE).getInt("cartMode", 1);
    }

    public static void saveCartMode(Context context, Integer value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("cartMode", value);
        editor.apply();
    }

    public static void saveDate(Context context, Integer value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("date", value);
        editor.apply();
    }

    public static Integer loadDate(Context context) {
        return context.getSharedPreferences("fridgex", Context.MODE_PRIVATE).getInt("date", 0);
    }

    public static void saveDailyRecipe(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String loadDailyRecipe(Context context, String key) {
        return context.getSharedPreferences("fridgex", Context.MODE_PRIVATE).getString(key, "");
    }

    public static void saveFirstStart(Context context, Boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("firstStart", value);
        editor.apply();
    }

    public static Boolean loadFirstStart(Context context) {
        return context.getSharedPreferences("fridgex", Context.MODE_PRIVATE).getBoolean("firstStart", true);
    }

    public static void saveTheme(Context context, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("theme", value);
        editor.apply();
    }

    public static String loadTheme(Context context) {
        return context.getSharedPreferences("fridgex", Context.MODE_PRIVATE).getString("theme", "def");
    }

    public static void saveFont(Context context, Float value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("font", value);
        editor.apply();
    }

    public static Float loadFont(Context context) {
        return context.getSharedPreferences("fridgex", Context.MODE_PRIVATE).getFloat("font", 1.0f);
    }
}