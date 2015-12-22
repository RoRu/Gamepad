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

    public  var robotsNum1      = 0
    public  var robotsNum2      = 0
    private var robots1         = Array(0)
                                { RectF(0f, 0f, 0f, 0f) }
    private var robots2         = Array(0)
                                { RectF(0f, 0f, 0f, 0f) }
    private var curCoords       = IntArray(0)
    private val roboPaint1      = Paint()
    private val roboPaint2      = Paint()
    private val p               = Paint()
    private val selectedPaint   = Paint()
    private var robotSelected   = -1
    private val netClient       = Client(ipSended)
    private var moveToCoords    = Array(0) { "" }
    private var messageToServer = ""
    private var firstRun        = true
    private val roboRadius      : Float
    private val eX              : Float
    private val eY              : Float
    private val ghostRobot      = RectF(0f, 0f, 0f, 0f)
    private val ghostPaint      = Paint()
    /*private var ghostX          = 0f
    private var ghostY          = 0f*/
    var startInRob = false
    /*private var txt = "It's me!"
    private val tPaint = Paint()*/

    init {
        roboPaint1.color = Color.BLUE
        roboPaint2.color = Color.RED
        /*tPaint.color = Color.BLACK
        tPaint.textSize = 36f;
        tPaint.style = Paint.Style.STROKE*/
        selectedPaint.color = Color.YELLOW
        ghostPaint.color = Color.YELLOW
        ghostPaint.alpha = 125

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
        //canvas.drawText(txt, 600f, 700f, tPaint)
        for(r in robots1)
            canvas.drawRoundRect(r, 150f, 150f, roboPaint1)
        if(robotSelected != -1) {
            canvas.drawRoundRect(robots1[robotSelected], 150f, 150f, selectedPaint)
            canvas.drawRoundRect(ghostRobot, 150f, 150f, ghostPaint)
        }
        for(r in robots2)
            canvas.drawRoundRect(r, 150f, 150f, roboPaint2)

    }

    public override fun onTouchEvent(event: MotionEvent): Boolean {

       /* val newX = event.x
        val newY = event.y
        if(event.action == MotionEvent.ACTION_DOWN) {
            for (i in robots1.indices) {
                if (robots1[i].contains(newX, newY)) {
                    robotSelected = i
                    return true
                }
            }
        }
        if(robotSelected != -1) {
            robots1[robotSelected].offsetTo(newX - roboRadius, newY - roboRadius)
            curCoords[robotSelected * 3 + 1] = (newX / eX).toInt()
            curCoords[robotSelected * 3 + 2] = (newY / eY).toInt()
            messageToServer  = curCoords.joinToString(" ")
            netClient.toSend = true
            invalidate()
        }
        return false*/
        val newX = event.x
        val newY = event.y
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                for (i in robots1.indices) {
                    if (robots1[i].contains(newX, newY)) {
                        robotSelected = i
                        startInRob = true
                        ghostRobot.set(robots1[i])
                  //      return true
                    }
                }
                //startInRob = false
            }
            MotionEvent.ACTION_MOVE -> {
                if(startInRob) {
                    ghostRobot.offsetTo(newX - roboRadius, newY - roboRadius)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if(startInRob) {
                    startInRob = false
                    moveToCoords[robotSelected] = "${robotSelected + 1} ${(newX / eX).toInt()} ${(newY / eY).toInt()}"
                    messageToServer = "$robotsNum1 ${moveToCoords.joinToString(" ")}"
                    netClient.toSend = true
                }
            }
        }
        return true
    }

    private fun setCoordinates(crds: String) {
        val sz: Int = crds.count { c -> c == ' ' }
        curCoords = IntArray(sz + 1)
        stringParse(crds)
        robotsNum1 = curCoords[0]
        moveToCoords = Array(robotsNum1) { "" }
        robots1 = Array(robotsNum1) { RectF(0f, 0f, 0f, 0f) }
        robotsNum2 = curCoords[robotsNum1 * 3 + 1]
        robots2 = Array(robotsNum2) { RectF(0f, 0f, 0f, 0f) }

        for(i in 2..robotsNum1 * 3 - 1 step 3) {
            var left = curCoords[ i ] * eX - roboRadius
            var top  = curCoords[i+1] * eY - roboRadius
            var ind  = curCoords[i-1] - 1
            robots1[ind].set(left, top, left + 2 * roboRadius,
                    top  + 2 * roboRadius)
            moveToCoords[ind] = "${ind + 1} ${curCoords[i]} ${curCoords[i + 1]}"
        }

        for(i in robotsNum1*3+3..curCoords.size - 2 step 3) {
            val left = curCoords[ i ] * eX - roboRadius
            val top  = curCoords[i+1] * eY - roboRadius
            robots2[curCoords[i-1] - 1].set(left, top, left + 2 * roboRadius,
                    top  + 2 * roboRadius)
        }
        invalidate()
    }

    private fun updateCanvas(newCoords: String) {
        //txt = newCoords
        stringParse(newCoords)
        for(i in 2..robotsNum1 * 3 - 1 step 3) {
            val left = curCoords[ i ] * eX - roboRadius
            val top  = curCoords[i+1] * eY - roboRadius
            robots1[curCoords[i-1] - 1].set(left, top, left + 2 * roboRadius,
                    top  + 2 * roboRadius)
        }

        for(i in robotsNum1*3+3..curCoords.size - 2 step 3) {
            val left = curCoords[ i ] * eX - roboRadius
            val top  = curCoords[i+1] * eY - roboRadius
            robots2[curCoords[i-1] - 1].set(left, top, left + 2 * roboRadius,
                    top  + 2 * roboRadius)
        }
        invalidate()
    }

    private fun stringParse(message: String) {
        val mesSplit = message.split(" ")
        for(i in curCoords.indices)
            curCoords[i] = mesSplit[i].toInt()
    }



    //==========================================================
    inner class Client(private val server_ip: String) : AsyncTask<Void, String, String>() {
        private val server_port  = 8080
        public  var toSend       = false
        private var first        = true

        protected override fun doInBackground(vararg arg0: Void): String {
            var socket = Socket()
            try {
                socket = Socket(server_ip, server_port)
            } catch (e: UnknownHostException) { e.printStackTrace()
            } catch (e: IOException) { e.printStackTrace() }

            try {
                val sout = socket.outputStream
                val sin  = socket.inputStream.bufferedReader()
                publishProgress(sin.readLine())
                while(true) {
                    if(toSend) sout.write(messageToServer.toByteArray())
                    publishProgress(sin.readLine())
                }
            } catch (e: IOException) { e.printStackTrace() }

            return ""
        }

        protected override fun onProgressUpdate(vararg values: String) {
            if(first) {
                setCoordinates(values[0].dropLast(1))
                first = false
                return
            }
            updateCanvas(values[0].dropLast(1))
        }

        protected override fun onPostExecute(result: String) { }
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
}
