package com.progix.fridgex.light.data

import android.content.Context

object SharedPreferencesAccess {

    fun saveBoolean(context: Context, key: String?, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun loadBoolean(context: Context, key: String?): Boolean {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, true)
    }

    fun saveString(context: Context, key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun loadString(context: Context, key: String): String? {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, "0")
    }

    fun loadNightMode(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("mode", 2)
    }

    fun saveNightMode(context: Context, value: Int) {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putInt("mode", value)
        editor?.apply()
    }

    fun loadCartMode(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("cartMode", 1)
    }

    fun saveCartMode(context: Context, value: Int) {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putInt("cartMode", value)
        editor?.apply()
    }

    fun saveDate(context: Context, value: Int) {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putInt("date", value)
        editor?.apply()
    }

    fun loadDate(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("date", 0)
    }

    fun saveDailyRecipe(context: Context, key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun loadDailyRecipe(context: Context, key: String): String? {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, "")
    }

    fun saveFirstStart(context: Context, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("firstStart", value)
        editor.apply()
    }

    fun loadFirstStart(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("firstStart", true)
    }

    fun saveTheme(context: Context, value: String) {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("theme", value)
        editor.apply()
    }

    fun loadTheme(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getString("theme", "def")
    }

    fun saveFont(context: Context, value: Float) {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("font", value)
        editor.apply()
    }

    fun loadFont(context: Context): Float {
        val sharedPreferences = context.getSharedPreferences("fridgex", Context.MODE_PRIVATE)
        return sharedPreferences.getFloat("font", 1.0f)
    }
}