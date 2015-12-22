package com.nutrix.gamepad.app

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class AboutActivity : Activity() {
    private  var text: TextView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }
}