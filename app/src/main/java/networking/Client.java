package networking;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.AsyncTask;

public class Client extends AsyncTask <Void, Void, String> {
    private String serverIP     = "192.168.0.105";
    private int    serverPort   = 8080;
    private String cmdLine      = null;
    private String data         = null;

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
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream sout = socket.getOutputStream();
            while(true) {
                cmdLine = br.readLine();
                data = cmdLine;
                System.out.println("The server was very polite. It sent me this : " + cmdLine);
            }
        }
        catch (IOException e) {}

    return null;
    }

    @Override
    protected void onPostExecute(String result) {
        Client.this.data = result;
    }
}