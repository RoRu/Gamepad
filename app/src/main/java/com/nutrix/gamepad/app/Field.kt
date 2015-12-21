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

class Field(content: Context, val ipSended: String) : View(content) {

    public  var robotsNum       = 2
    private val robots          = Array(robotsNum)
                                { RectF(0f, 0f, 0f, 0f) }
    private val coords          = IntArray(robotsNum*3)
    private val roboPaint       = Paint()
    private var p               = Paint()
    private var robotSelected   = -1
    private val netClient       = Client(ipSended)
    private var messageToServer = ""
    private var firstRun        = true
    private val roboRadius      : Float
    private val eX              : Float
    private val eY              : Float

    init {
        roboPaint.color = Color.BLUE

        val dispM  = context.resources.displayMetrics
        roboRadius = dispM.heightPixels.toFloat() / 20f
        eY = dispM.heightPixels.toFloat() / 480f
        eX = dispM.widthPixels.toFloat() / 640f

        isFocusableInTouchMode = true
        isClickable            = true
        //isLongClickable        = false
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
        p.color = Color.WHITE
        p.style = Paint.Style.STROKE
        val height = height
        val width  = width
        canvas.drawLine(20f, 20f, 20f, height - 20f, p)
        canvas.drawLine(width - 20f, 20f, width - 20f, height - 20f, p)
        canvas.drawLine(width - 20f, 20f, 20f, 20f, p)
        canvas.drawLine(20f, height - 20f, width - 20f, height - 20f, p)
        canvas.drawLine((width - 40f) / 2 + 20f, 20f, (width - 40f) / 2 + 20f, height - 20f, p)
        canvas.drawCircle(width/2f, height / 2f, height / 8f, p)

        canvas.drawLine(10f, height / 2f - 15f, 20f, height / 2f - 15f, p)
        canvas.drawLine(10f, height / 2f + 15f, 20f, height / 2f + 15f, p)
        canvas.drawLine(10f, height / 2f - 15f, 10f, height / 2f + 15f, p)

        canvas.drawLine(width - 10f, height / 2f - 15f, width - 20f, height / 2f - 15f, p)
        canvas.drawLine(width - 10f, height / 2f + 15f, width - 20f, height / 2f + 15f, p)
        canvas.drawLine(width - 10f, height / 2f - 15f, width - 10f, height / 2f + 15f, p)

        canvas.drawLine(20f, height / 2f - 30f, 40f, height / 2f - 30f, p)
        canvas.drawLine(20f, height / 2f + 30f, 40f, height / 2f + 30f, p)
        canvas.drawLine(40f, height / 2f - 30f, 40f, height / 2f + 30f, p)

        canvas.drawLine(width - 20f, height / 2f - 30f, width - 40f, height / 2f - 30f, p)
        canvas.drawLine(width - 20f, height / 2f + 30f, width - 40f, height / 2f + 30f, p)
        canvas.drawLine(width - 40f, height / 2f - 30f, width - 40f, height / 2f + 30f, p)

        canvas.drawLine(20f, height / 2f - 100f, 120f, height / 2f - 100f, p)
        canvas.drawLine(20f, height / 2f + 100f, 120f, height / 2f + 100f, p)
        canvas.drawLine(120f, height / 2f - 100f, 120f, height / 2f + 100f, p)

        canvas.drawLine(width - 20f, height / 2f - 100f, width - 120f, height / 2f - 100f, p)
        canvas.drawLine(width - 20f, height / 2f + 100f, width - 120f, height / 2f + 100f, p)
        canvas.drawLine(width - 120f, height / 2f - 100f, width - 120f, height / 2f + 100f, p)
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
    inner class Client(server_ip: String) : AsyncTask<Void, String, String>() {
        public  var SERVER_IP   = server_ip
        private val SERVER_PORT  = 8080
        public  var toSend       = false

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