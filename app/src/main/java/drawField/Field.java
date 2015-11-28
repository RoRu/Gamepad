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
    private float[] coord;
    private Paint roboPaint;
    private Paint textPaint;
    private float eX;
    private float eY;
    private int robotSelected;
    private float roboRadius;
    private String txt;
    private boolean ifFirstRun;
    private MyClient netClient;



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

        coord = new float[]{0, 100, 200, 1, 200, 300};//, 2, 300, 400, 3, 500, 400, 4, 500, 100};
        robots = new RectF[coord.length / 3];
        for(int i = 1; i < coord.length - 1; i+=3) {
            float left = coord[i] * eX - roboRadius;
            float top  = coord[i+1] * eY - roboRadius;
            robots[(i-1)/3] = new RectF(left, top, left + 2*roboRadius, top + 2*roboRadius);
        }


        setFocusableInTouchMode(true);
        setClickable(true);
        setLongClickable(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            /*float newX;
            float newY;
            newX = event.getX();
            newY = event.getY();*/
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
            txt = Integer.toString(robotSelected);
            invalidate();
            return true;
        }
        return false;
    }

    private void updateCanvas(byte[] newCoords) {
        stringParse(newCoords);
        updateRobotsCoordinates();
        invalidate();
    }

    private void updateRobotsCoordinates() {
        for(int i = 1; i < coord.length - 1; i+=3) {
            float left = coord[i] * eX - roboRadius;
            float top  = coord[i+1] * eY - roboRadius;
            //robots[(i-1)/3] = new RectF(left, top, left + 2*roboRadius, top + 2*roboRadius);
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

    private void stringParse(byte[] st) {
        byte[] b = new byte[st.length+1];
        for (int i = 0; i < b.length-1; i++) {
            b[i] = st[i];
        }
        b[st.length] = 32;
        String sToFloat = "";
        //float[] res = new float[6];
        int lastSpace = 1;
        coord[0] = b[0] - 48;
        int count = 1;
        for (int i = 2; i < b.length; i++) {
            if(b[i] == 32) {
                for(int j = lastSpace+1; j < i; j++) {
                    sToFloat += (char)b[j];
                    //System.out.println("Debug: " + b[j]);
                }
                coord[count] = Float.parseFloat(sToFloat);
                //System.out.println("Debug: " + sToFloat);
                sToFloat = "";
                lastSpace = i;
                count++;
            }
        }
    }
    private void stringParse(String mes) {
        String[] spl = mes.split(" ");
        for (int i = 0; i < coord.length; i++) {
            coord[i] = Float.parseFloat(spl[i]);
        }
    }

    public byte[] floatParse(float[] fl) {
        StringBuilder sb = new StringBuilder();
        for (float f : fl) {
            sb.append((int)f);
            sb.append(" ");
        }
        return sb.toString().getBytes();
    }

    //==================================================================
    private class MyClient extends AsyncTask <Void, byte[], String> {
        private String serverIP          = "192.168.0.203";
        private int    serverPort        = 8080;
        private String messageFromServer = null;
        private String data              = null;

        @Override
        protected String doInBackground(Void... arg0) {
            Socket socket = null;
            try {
                socket = new Socket(serverIP, serverPort);
                //return "YEA";
                //txt = "Success!";
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                InputStream sin = socket.getInputStream();
                OutputStream sout = socket.getOutputStream();
                byte[] coordMessage = new byte[19];
                //BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                sout.write(49);
                while(true) {
                    //messageFromServer = in.readLine();
                    //data = messageFromServer;
                    sin.read(coordMessage);
                    publishProgress(coordMessage);
                }
            }
            catch (IOException e) { }

            return null;
        }

        @Override
        protected void onProgressUpdate(byte[]... values) {
            updateCanvas(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            //this.data = result;
        }
    }
}
