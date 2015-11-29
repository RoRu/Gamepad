package com.nutrix.gamepad.app

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import drawField.Field

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // settings for fullscreen and landscape orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(Field(this))
        //new Client().execute();
    }
}
