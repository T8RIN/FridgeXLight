package com.progix.fridgex.light.custom

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.progix.fridgex.light.application.FridgeXLightApplication

abstract class ApplicationBindedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as FridgeXLightApplication).setCurrentContext(this)
    }

    override fun onStart() {
        super.onStart()
        (applicationContext as FridgeXLightApplication).setCurrentContext(this)
    }

    override fun onResume() {
        super.onResume()
        (applicationContext as FridgeXLightApplication).setCurrentContext(this)
    }

}