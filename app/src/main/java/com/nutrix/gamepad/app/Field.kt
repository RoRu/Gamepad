package com.nutrix.gamepad.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import java.io.*
import java.net.Socket
import java.net.UnknownHostException
import android.os.AsyncTask
import kotlin.IntArray

class Field(content: Context) : View(content) {

    var ROBOTS_NUM = 2
    private val robots: Array<RectF>
    private val coords: IntArray
    private val roboPaint: Paint
    private val roboRadius: Float
    private var robotSelected: Int = 0
    private val eX: Float
    private val eY: Float
    private val netClient: Client
    private var messageToServer: ByteArray? = null
    private var firstRun: Boolean = false

    init {
        roboPaint = Paint()
        roboPaint.color = Color.BLUE

        val dispM = context.resources.displayMetrics
        roboRadius = (dispM.heightPixels / 20).toFloat()
        robotSelected = -1
        eY = dispM.heightPixels.toFloat() / 480
        eX = dispM.widthPixels.toFloat() / 640
        netClient = Client()
        firstRun = true

        coords = intArrayOf(1, 0, 0, 2, 0, 0)
        robots = Array(ROBOTS_NUM) { RectF(0f, 0f, 10f, 10f) }

        isFocusableInTouchMode = true
        isClickable = true
        isLongClickable = false
    }


    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (firstRun) {
            netClient.execute()
            firstRun = false
        }
        canvas.drawColor(Color.rgb(0, 139, 69))
        for (r in robots)
            canvas.drawRoundRect(r, 150f, 150f, roboPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val newX = event.x
        val newY = event.y
        if (event.action == MotionEvent.ACTION_DOWN) {
            for (i in robots.indices) {
                if (robots[i].contains(newX, newY)) {
                    robotSelected = i
                    return true
                }
            }
        }
        if (robotSelected != -1) {
            robots[robotSelected].offsetTo(newX - 50, newY - 50)
            coords[robotSelected * 3 + 1] = (newX / eX).toInt()
            coords[robotSelected * 3 + 2] = (newY / eY).toInt()
            intParse(coords)
            netClient.toSend = true
            invalidate() // probably we won't need that
            //due to the server sending
            //messages to us constantly;
        }
        return false
    }

    private fun updateCanvas(newCoords: String) {
        stringParse(newCoords)
        var i = 1
        while (i < coords.size() - 1) {
            val left = coords[i] * eX - roboRadius
            val top = coords[i + 1] * eY - roboRadius
            robots[(i - 1) / 3].left = left
            robots[(i - 1) / 3].right = left + 2 * roboRadius
            robots[(i - 1) / 3].top = top
            robots[(i - 1) / 3].bottom = top + 2 * roboRadius
            i += 3
        }
        invalidate()
    }

    private fun stringParse(message: String) {
        val mesSplit = message.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        for (i in coords.indices) {
            coords[i] = Integer.parseInt(mesSplit[i])
        }
    }

    private fun intParse(coordinates: IntArray) {
        val res = StringBuilder()
        var i = 0
        while (i < coordinates.size() - 2) {
            res.append(coordinates[i])
            res.append(" ")
            res.append(coordinates[i + 1])
            res.append(" ")
            res.append(coordinates[i + 2])
            res.append(" ")
            i += 3
        }
        res.deleteCharAt(res.length() - 1)
        messageToServer = res.toString().toByteArray()
    }


    //==========================================================
    private inner class Client : AsyncTask<Void, String, String>() {
        private val SERVER_IP = "192.168.0.206"
        private val SERVER_PORT = 8080
        public  var toSend = false

        override fun doInBackground(vararg arg0: Void): String {
            var socket = Socket()
            try {
                socket = Socket(SERVER_IP, SERVER_PORT)
            } catch (e: UnknownHostException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            try {
                val sout = socket.outputStream
                val `in` = BufferedReader(
                        InputStreamReader(socket.inputStream))
                while (true) {
                    if (toSend) sout.write(messageToServer)
                    publishProgress(`in`.readLine())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return ""
        }

        override fun onProgressUpdate(vararg values: String) {
            updateCanvas(values[0])
        }

        override fun onPostExecute(result: String) {
        }
    }
}
