package com.progix.fridgex.light

import android.app.Application
import android.content.Context

class FridgeXLightApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }

    companion object {
        lateinit var appContext: Context
    }

}