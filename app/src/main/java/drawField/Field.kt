package drawField

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


class Field(context: Context) : View(context) {

    private val robots: Array<RectF>
    private val coords: FloatArray
    private val roboPaint: Paint
    private val textPaint: Paint
    private val eX: Float
    private val eY: Float
    private var robotSelected: Int = 0
    private val roboRadius: Float
    private var txt: String? = null
    private var ifFirstRun: Boolean = false
    private var netClient: MyClient
    private var messageToServer: ByteArray? = null

    init {
        ifFirstRun = true
        roboPaint = Paint()
        roboPaint.color = Color.BLUE
        textPaint = Paint()
        netClient = MyClient()


        robotSelected = -1
        val dispM = getContext().resources.displayMetrics
        eY = dispM.heightPixels.toFloat() / 480
        eX = dispM.widthPixels.toFloat() / 640
        roboRadius = (dispM.heightPixels / 20).toFloat()
        txt = java.lang.Float.toString(eX) + "\n" + java.lang.Float.toString(eY)

        coords = floatArrayOf(0, 100, 200, 1, 200, 300)//, 2, 300, 400, 3, 500, 400, 4, 500, 100};
        robots = Array<RectF>(coords.size / 3, {i -> 0})

        var i = 1
        while (i < coords.size - 1) {
            val left = coords[i] * eX - roboRadius
            val top = coords[i + 1] * eY - roboRadius
            robots[(i - 1) / 3] = RectF(left, top, left + 2 * roboRadius, top + 2 * roboRadius)
            i += 3
        }

        isFocusableInTouchMode = true
        isClickable = true
        isLongClickable = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            onTouch(event.x, event.y)
        }
        return false
    }

    private fun onTouch(newX: Float, newY: Float): Boolean {
        for (index in 0..robots.size - 1) {
            if (robots[index].contains(newX, newY)) {
                robotSelected = index
                return true
            }
        }
        if (robotSelected > -1) {
            robots[robotSelected].offsetTo(newX - 50, newY - 50)
            coords[robotSelected * 3 + 1] = newX / eX
            coords[robotSelected * 3 + 2] = newY / eY
            messageToServer = floatParse(coords)
            txt = java.lang.Float.toString(coords[1])
            netClient.toSend = true
            /*try {
                txt = new String(messageToServer, "Cp1252");
            } catch (UnsupportedEncodingException e) {}*/
            invalidate()
            return true
        }
        return false
    }

    private fun updateCanvas(newCoords: String) {
        stringParse(newCoords)
        updateCoordinatesOnMessage()
        invalidate()
    }


    private fun updateCoordinatesOnMessage() {
        var i = 1
        while (i < coords.size - 1) {
            val left = coords[i] * eX - roboRadius
            val top = coords[i + 1] * eY - roboRadius
            robots[(i - 1) / 3].left = left
            robots[(i - 1) / 3].right = left + 2 * roboRadius
            robots[(i - 1) / 3].top = top
            robots[(i - 1) / 3].bottom = top + 2 * roboRadius
            i += 3
        }

    }

    public override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (ifFirstRun) {
            netClient.execute()
            ifFirstRun = false
        }
        canvas!!.drawColor(Color.rgb(0, 139, 69))
        textPaint.color = Color.BLACK
        textPaint.textSize = 60f
        canvas.drawText(txt, 500f, 500f, textPaint)
        for (r in robots) {
            canvas.drawRoundRect(r, 150f, 150f, roboPaint)
        }
    }

    private fun stringParse(mes: String) {
        val spl = mes.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        for (index in 0..coords.size - 1) {
            coords[index] = java.lang.Float.parseFloat(spl[index])
        }
    }

    fun floatParse(fl: FloatArray):String {
        val sb = StringBuilder()
        var i = 0
        while (i < fl.size - 2) {
            sb.append(fl[i].toInt())
            sb.append(" ")
            sb.append(fl[i + 1].toInt())
            sb.append(" ")
            sb.append(fl[i + 2].toInt())
            sb.append(" ")
            i += 3
        }
        sb.deleteCharAt(sb.length - 1)
        return sb.toString() //here must be bytes
    }


    //==================================================================
    public inner class MyClient : AsyncTask<Void, String, String>() {
        private val serverIP = "192.168.0.105"
        private val serverPort = 8080
        private var messageFromServer: String? = null
        //private String data              = null;
        public  var toSend = false
        //private String messageToSend     = "";


        override fun doInBackground(vararg arg0: Void): String? {
            var socket: Socket? = null
            try {
                socket = Socket(serverIP, serverPort)
            } catch (e: UnknownHostException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            try {
                //InputStream sin = socket.getInputStream();
                val sout = socket!!.outputStream
                //byte[] coordMessage = new byte[19];
                val `in` = BufferedReader(InputStreamReader(socket.inputStream))
                //sout.write(49);
                while (true) {
                    if (toSend) {
                        sout.write(messageToServer)
                    }
                    messageFromServer = `in`.readLine()
                    publishProgress(messageFromServer)
                    //data = messageFromServer;
                    //sin.read(coordMessage);
                    //publishProgress(coordMessage);
                }
            } catch (e: IOException) {
            }

            return null
        }

        override fun onProgressUpdate(vararg values: String) {
            updateCanvas(values[0])
        }

        override fun onPostExecute(result: String?) {
            //this.data = result;
        }
    }
}