package com.progix.fridgex.light

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.progix.fridgex.light.data.SharedPreferencesAccess


class FridgeXLightApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this
        when (SharedPreferencesAccess.loadNightMode(this)) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        heightPixels = resources.displayMetrics.heightPixels
        widthPixels = resources.displayMetrics.widthPixels
    }

    companion object {
        lateinit var appContext: Context
        var heightPixels: Int = 0
        var widthPixels: Int = 0
    }

    private var mCurrentContext: Context? = null
    fun getCurrentContext(): Context? {
        return mCurrentContext
    }

    fun setCurrentContext(mCurrentContext: Context?) {
        this.mCurrentContext = mCurrentContext
    }

}