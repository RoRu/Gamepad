package com.nutrix.gamepad.app
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager

class ActivityField : Activity() {
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val fuct = Intent(applicationContext, MainActivity::class.java)
        val extras = intent.extras
        var ipAdr = ""
        if(extras != null) {
            ipAdr = extras.getString("ipConf")
        }

        setContentView(Field(this, ipAdr))
    }
}