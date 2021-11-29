package com.progix.fridgex.light

import android.app.Application
import android.content.Context
import android.app.Activity




class FridgeXLightApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }

    companion object {
        lateinit var appContext: Context
    }

    private var mCurrentContext: Context? = null
    fun getCurrentContext(): Context? {
        return mCurrentContext
    }

    fun setCurrentContext(mCurrentContext: Context?) {
        this.mCurrentContext = mCurrentContext
    }

}