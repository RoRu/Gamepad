package networking;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.AsyncTask;

public class Client {//extends AsyncTask <Void, Void, String> {
    /*private String serverIP          = "192.168.0.203";
    private int    serverPort        = 8080;
    private String messageFromServer = null;
    private String data              = null;

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
            BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream   out = socket.getOutputStream();
            while(true) {
                messageFromServer = in.readLine();
                data = messageFromServer;
            }
        }
        catch (IOException e) {}

    return null;
    }

    @Override
    protected void onPostExecute(String result) {
        this.data = result;
    }*/
}
