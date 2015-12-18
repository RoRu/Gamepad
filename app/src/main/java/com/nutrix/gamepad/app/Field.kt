package com.nutrix.gamepad.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import java.io.*
import java.net.Socket
import java.net.UnknownHostException
import android.os.AsyncTask
import kotlin.IntArray

class Field(content: Context) : View(content) {

    public  var robotsNum       = 2
    private val robots          = Array(robotsNum)
    { RectF(0f, 0f, 0f, 0f) }
    private val coords          = IntArray(robotsNum*3)
    private val roboPaint       = Paint()
    private var p               = Paint()
    private var robotSelected   = -1
    private val netClient       = Client()
    private var messageToServer = ""
    private var firstRun        = true
    private val roboRadius      : Float
    private val eX              : Float
    private val eY              : Float
    var heightDisplay: Int = 0
    var widthDisplay: Int = 0

    init {
        roboPaint.color = Color.BLUE

        val dispM  = context.resources.displayMetrics
        roboRadius = dispM.heightPixels.toFloat() / 20f
        eY = dispM.heightPixels.toFloat() / 480f
        eX = dispM.widthPixels.toFloat() / 640f

        isFocusableInTouchMode = true
        isClickable            = true
        isLongClickable        = false
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(firstRun) {
            netClient.execute()
            firstRun = false
        }

        canvas.drawColor(Color.rgb(0, 139, 69))
        drawField(canvas, p)

        for(r in robots)
            canvas.drawRoundRect(r, 150f, 150f, roboPaint)
    }

    public fun drawField(canvas: Canvas, p: Paint) {
        //DRAW FIELD
        p.setColor(Color.WHITE)
        p.setStyle(Paint.Style.STROKE)
        heightDisplay = getHeight()
        widthDisplay = getWidth()
        canvas.drawLine(20.toFloat(), 20.toFloat(), 20.toFloat(), heightDisplay.toFloat() - 20.toFloat(), p)
        canvas.drawLine(widthDisplay.toFloat() - 20.toFloat(), 20.toFloat(), widthDisplay.toFloat() - 20.toFloat(), heightDisplay.toFloat() - 20.toFloat(), p)
        canvas.drawLine(widthDisplay.toFloat() - 20.toFloat(), 20.toFloat(), 20.toFloat(), 20.toFloat(), p)
        canvas.drawLine(20.toFloat(), heightDisplay.toFloat() - 20.toFloat(), widthDisplay.toFloat() - 20.toFloat(), heightDisplay.toFloat() - 20.toFloat(), p)
        canvas.drawLine((widthDisplay.toFloat() - 40.toFloat()) / 2 + 20.toFloat(), 20.toFloat(), (widthDisplay.toFloat() - 40.toFloat()) / 2 + 20.toFloat(), heightDisplay.toFloat() - 20.toFloat(), p)
        canvas.drawCircle(widthDisplay.toFloat()/2.toFloat(), heightDisplay.toFloat() / 2.toFloat(), heightDisplay.toFloat()/8, p)

        canvas.drawLine(10.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 15.toFloat(), 20.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 15.toFloat(), p)
        canvas.drawLine(10.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 15.toFloat(), 20.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 15.toFloat(), p)
        canvas.drawLine(10.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 15.toFloat(), 10.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 15.toFloat(), p)

        canvas.drawLine(widthDisplay.toFloat() - 10.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 15.toFloat(), widthDisplay.toFloat() - 20.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 15.toFloat(), p)
        canvas.drawLine(widthDisplay.toFloat() - 10.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 15.toFloat(), widthDisplay.toFloat() - 20.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 15.toFloat(), p)
        canvas.drawLine(widthDisplay.toFloat() - 10.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 15.toFloat(), widthDisplay.toFloat() - 10.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 15.toFloat(), p)

        canvas.drawLine(20.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 30.toFloat(), 40.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 30.toFloat(), p)
        canvas.drawLine(20.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 30.toFloat(), 40.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 30.toFloat(), p)
        canvas.drawLine(40.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 30.toFloat(), 40.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 30.toFloat(), p)

        canvas.drawLine(widthDisplay.toFloat() - 20.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 30.toFloat(), widthDisplay.toFloat() - 40.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 30.toFloat(), p)
        canvas.drawLine(widthDisplay.toFloat() - 20.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 30.toFloat(), widthDisplay.toFloat() - 40.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 30.toFloat(), p)
        canvas.drawLine(widthDisplay.toFloat() - 40.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 30.toFloat(), widthDisplay.toFloat() - 40.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 30.toFloat(), p)

        canvas.drawLine(20.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 100.toFloat(), 120.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 100.toFloat(), p)
        canvas.drawLine(20.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 100.toFloat(), 120.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 100.toFloat(), p)
        canvas.drawLine(120.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 100.toFloat(), 120.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 100.toFloat(), p)

        canvas.drawLine(widthDisplay.toFloat() - 20.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 100.toFloat(), widthDisplay.toFloat() - 120.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 100.toFloat(), p)
        canvas.drawLine(widthDisplay.toFloat() - 20.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 100.toFloat(), widthDisplay.toFloat() - 120.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 100.toFloat(), p)
        canvas.drawLine(widthDisplay.toFloat() - 120.toFloat(), heightDisplay.toFloat() / 2.toFloat() - 100.toFloat(), widthDisplay.toFloat() - 120.toFloat(), heightDisplay.toFloat() / 2.toFloat() + 100.toFloat(), p)
    }

    public override fun onTouchEvent(event: MotionEvent): Boolean {
        val newX = event.x
        val newY = event.y
        if(event.action == MotionEvent.ACTION_DOWN) {
            for (i in robots.indices) {
                if (robots[i].contains(newX, newY)) {
                    robotSelected = i
                    return true
                }
            }
        }
        if(robotSelected != -1) {
            robots[robotSelected].offsetTo(newX - roboRadius, newY - roboRadius)
            coords[robotSelected * 3 + 1] = (newX / eX).toInt()
            coords[robotSelected * 3 + 2] = (newY / eY).toInt()
            messageToServer  = coords.joinToString(" ")
            netClient.toSend = true
            invalidate()
        }
        return false
    }

    private fun updateCanvas(newCoords: String) {
        stringParse(newCoords)
        for(i in 1..coords.size - 2 step 3) {
            val left = coords[ i ] * eX - roboRadius
            val top  = coords[i+1] * eY - roboRadius
            robots[(i-1)/3].set(left, top, left + 2 * roboRadius,
                    top  + 2 * roboRadius)
        }
        invalidate()
    }

    private fun stringParse(message: String) {
        val mesSplit = message.split(" ")
        for(i in coords.indices)
            coords[i] = mesSplit[i].toInt()
    }


    //==========================================================
    private inner class Client : AsyncTask<Void, String, String>() {
        private val SERVER_IP   = "192.168.0.103"
        private val SERVER_PORT = 8080
        public  var toSend      = false

        protected override fun doInBackground(vararg arg0: Void): String {
            var socket = Socket()
            try {
                socket = Socket(SERVER_IP, SERVER_PORT)
            } catch (e: UnknownHostException) { e.printStackTrace()
            } catch (e: IOException) { e.printStackTrace() }

            try {
                val sout = socket.outputStream
                val sin  = socket.inputStream.bufferedReader()
                while(true) {
                    if(toSend) sout.write(messageToServer.toByteArray())
                    publishProgress(sin.readLine())
                }
            } catch (e: IOException) { e.printStackTrace() }

            return ""
        }

        protected override fun onProgressUpdate(vararg values: String) {
            updateCanvas(values[0])
        }

        protected override fun onPostExecute(result: String) { }
    }
}