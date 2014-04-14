package com.lwm.app.server.async;

import android.os.AsyncTask;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.server.StreamServer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Set;

public class ClientsManager {

    public static enum Command {PREPARE, PLAY, PAUSE, SEEK_TO}
    HttpClient httpclient = new DefaultHttpClient();
    HttpPost request;
    ResponseHandler<String> responseHandler = new BasicResponseHandler();
    Set<String> clients = StreamServer.getClients();
    Set<String> ready = StreamServer.getReadyClients();

    public void changeSong(){
        ready.clear();
        new CommandRunner().execute(Command.PREPARE);
        try {
            int i = 0;
            while(!clients.equals(ready) && i++<10) Thread.sleep(1000);
            if(i == 10){
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new CommandRunner().execute(Command.PLAY);
    }

    public void pause(){
        Log.d(App.TAG, "ClientsManager: pause");
        new CommandRunner().execute(Command.PAUSE);
    }

    public void start(){
        Log.d(App.TAG, "ClientsManager: start");
        new CommandRunner().execute(Command.PLAY);
    }

    private class CommandRunner extends AsyncTask<Command, Void, Void> {

        @Override
        protected Void doInBackground(Command... commands) {
            sendRequests(commands[0]);
            return null;
        }

        private void sendRequests(Command command) {
            String method;
            switch (command) {
                case PREPARE:
                    method = StreamServer.PREPARE;
                    break;
                case PLAY:
                    method = StreamServer.PLAY;
                    break;
                case PAUSE:
                    method = StreamServer.PAUSE;
                    break;
                case SEEK_TO:
                    method = StreamServer.SEEK_TO;
                    break;
                default:
                    return;
            }

            for (String client : clients) {
                request = new HttpPost("http://" + client + ":8888" + method);

                try {
                    String response = httpclient.execute(request, responseHandler);

                    //Debug
                    Log.d(App.TAG, "response: " + response);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}