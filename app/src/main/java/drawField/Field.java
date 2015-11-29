package drawField;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.AsyncTask;


public class Field extends View {

    private RectF[] robots;
    private float[] coords;
    private Paint roboPaint;
    private Paint textPaint;
    private float eX;
    private float eY;
    private int robotSelected;
    private float roboRadius;
    private String txt;
    private boolean ifFirstRun;
    private MyClient netClient;
    private byte[] messageToServer;



    public Field(Context context) {
        super(context);
        ifFirstRun = true;
        roboPaint = new Paint();
        roboPaint.setColor(Color.BLUE);
        textPaint = new Paint();
        netClient = new MyClient();


        robotSelected = -1;
        DisplayMetrics dispM = getContext().getResources().getDisplayMetrics();
        eY = (float)dispM.heightPixels / 480;
        eX = (float)dispM.widthPixels / 640;
        roboRadius = dispM.heightPixels / 20;
        txt = Float.toString(eX) + "\n" + Float.toString(eY);

        coords = new float[]{0, 100, 200, 1, 200, 300};//, 2, 300, 400, 3, 500, 400, 4, 500, 100};
        robots = new RectF[coords.length / 3];
        for(int i = 1; i < coords.length - 1; i+=3) {
            float left = coords[i] * eX - roboRadius;
            float top  = coords[i+1] * eY - roboRadius;
            robots[(i-1)/3] = new RectF(left, top, left + 2*roboRadius, top + 2*roboRadius);
        }


        setFocusableInTouchMode(true);
        setClickable(true);
        setLongClickable(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            onTouch(event.getX(), event.getY());
        }
        return false;
    }

    private boolean onTouch(float newX, float newY) {
        for (int i = 0; i < robots.length; i++) {
            if(robots[i].contains(newX, newY)) {
                robotSelected = i;
                return true;
            }
        }
        if (robotSelected > -1) {
            robots[robotSelected].offsetTo(newX - 50, newY - 50);
            coords[robotSelected*3+1] = newX / eX;
            coords[robotSelected*3+2] = newY / eY;
            messageToServer = floatParse(coords);
            txt = Float.toString(coords[1]);
            netClient.toSend = true;
            /*try {
                txt = new String(messageToServer, "Cp1252");
            } catch (UnsupportedEncodingException e) {}*/
            invalidate();
            return true;
        }
        return false;
    }

    private void updateCanvas(String newCoords) {
        stringParse(newCoords);
        updateCoordinatesOnMessage();
        invalidate();
    }
    
    
    private void updateCoordinatesOnMessage() {
        for(int i = 1; i < coords.length - 1; i+=3) {
            float left = coords[i] * eX - roboRadius;
            float top  = coords[i+1] * eY - roboRadius;
            robots[(i-1)/3].left = left;
            robots[(i-1)/3].right = left + 2*roboRadius;
            robots[(i-1)/3].top = top;
            robots[(i-1)/3].bottom = top + 2*roboRadius;
        }

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(ifFirstRun) {
            netClient.execute();
            ifFirstRun = false;
        }
        canvas.drawColor(Color.rgb(0, 139, 69));
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(60);
        canvas.drawText(txt, 500, 500, textPaint);
        for (RectF r : robots) {
            canvas.drawRoundRect(r, 150, 150, roboPaint);
        }
    }
    
    private void stringParse(String mes) {
        String[] spl = mes.split(" ");
        for (int i = 0; i < coords.length; i++) {
            coords[i] = Float.parseFloat(spl[i]);
        }
    }

    public byte[] floatParse(float[] fl) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fl.length - 2; i += 3) {
            sb.append((int)fl[i]);
            sb.append(" ");
            sb.append((int)fl[i+1]);
            sb.append(" ");
            sb.append((int)fl[i+2]);
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString().getBytes();
    }

    //==================================================================
    private class MyClient extends AsyncTask <Void, String, String> {
        private String serverIP          = "192.168.0.203";
        private int    serverPort        = 8080;
        private String messageFromServer = null;
        //private String data              = null;
        private boolean toSend           = false;
        //private String messageToSend     = "";


        @Override
        protected String doInBackground(Void... arg0) {
            Socket socket = null;
            try {
                socket = new Socket(serverIP, serverPort);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                //InputStream sin = socket.getInputStream();
                OutputStream sout = socket.getOutputStream();
                //byte[] coordMessage = new byte[19];
                BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //sout.write(49);
                while(true) {
                    if(toSend) {
                        sout.write(messageToServer);
                    }
                    messageFromServer = in.readLine();
                    publishProgress(messageFromServer);
                    //data = messageFromServer;
                    //sin.read(coordMessage);
                    //publishProgress(coordMessage);
                }
            }
            catch (IOException e) { }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            updateCanvas(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            //this.data = result;
        }
    }
}
