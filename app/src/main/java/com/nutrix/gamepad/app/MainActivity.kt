package com.nutrix.gamepad.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

public class MainActivity: Activity() {
    internal val context: Context = this
    private  var buttonIP: Button? = null
    private  var final_text: TextView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonStart = findViewById(R.id.buttonStart) as Button
        buttonStart.setOnClickListener {
            val SecAct = Intent(applicationContext, ActivityField::class.java)
            startActivity(SecAct)
        }

        buttonIP = findViewById(R.id.ip_button) as Button
        final_text = findViewById(R.id.final_text) as TextView

        buttonIP!!.setOnClickListener {
            val li = LayoutInflater.from(context)
            val promptsView = li.inflate(R.layout.prompt, null)

            val mDialogBuilder = AlertDialog.Builder(context)

            mDialogBuilder.setView(promptsView)

            val userInput = promptsView.findViewById(R.id.input_text) as EditText

            mDialogBuilder.setCancelable(false).setPositiveButton("OK"
            ) { dialog, id ->
                final_text!!.text = userInput.text
            }.setNegativeButton("Cancel"
            ) { dialog, id -> dialog.cancel() }

            val alertDialog = mDialogBuilder.create()
            alertDialog.show()
            intent.putExtra(userInput.toString(), 0)
        }
    }
}
