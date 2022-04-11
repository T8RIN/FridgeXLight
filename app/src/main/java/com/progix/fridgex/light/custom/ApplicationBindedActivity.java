package com.progix.fridgex.light.custom;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.progix.fridgex.light.application.FridgeXLightApplication;

import org.jetbrains.annotations.Nullable;

public abstract class ApplicationBindedActivity extends AppCompatActivity {
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FridgeXLightApplication) this.getApplicationContext()).setCurrentContext(this);
    }

    protected void onStart() {
        super.onStart();
        ((FridgeXLightApplication) this.getApplicationContext()).setCurrentContext(this);
    }

    protected void onResume() {
        super.onResume();
        ((FridgeXLightApplication) this.getApplicationContext()).setCurrentContext(this);
    }
}
