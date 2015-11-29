package networking;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.AsyncTask;
import drawField.Field;

public class Client {/*extends AsyncTask <Void, byte[], String> {
    private String serverIP               = "192.168.0.105";
    private int    serverPort             = 8080;
    private String messageFromServer      = null;
    private String data                   = null;
    Progress cord1;

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
    protected void onProgressUpdate(byte[]... values){
        cord1.update(values[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        this.data = result;
    }

    public  class Progress {
       String coord;
        public void update(byte[] newcoords) {
            coord = newcoords.toString();

        }
    }
*/
}