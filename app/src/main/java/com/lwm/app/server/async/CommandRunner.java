package com.lwm.app.server.async;

import android.os.AsyncTask;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.model.Client;
import com.lwm.app.server.StreamServer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class CommandRunner extends AsyncTask<CommandRunner.Command, Void, Void> {

    HttpClient httpclient = new DefaultHttpClient();
    HttpPost request;
    ResponseHandler<String> responseHandler = new BasicResponseHandler();

    Client client;

    public CommandRunner(Client client) {
        this.client = client;
    }

    public static enum Command { PREPARE, PLAY, PAUSE, SEEK_TO, PING }

    @Override
    protected Void doInBackground(CommandRunner.Command... commands) {
        for(Command command:commands){
            sendRequest(command);
        }
        return null;
    }

    private void sendRequest(Command command) {
        String method;
        if(Command.PING.equals(command)){
            request = new HttpPost("http://" + client.getIP() + ":8888" + StreamServer.PING);
            try {
                long start = System.currentTimeMillis();
                httpclient.execute(request, responseHandler);
                long ping = System.currentTimeMillis() - start;
                client.setPing(Math.round(ping/2.));

                // Debug
                Log.d(App.TAG, "Ping: "+client.getPing());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            switch (command) {
                case PREPARE:
                    Log.d(App.TAG, "CommandRunner: PREPARE");
                    method = StreamServer.PREPARE;
                    break;
                case PLAY:
                    Log.d(App.TAG, "CommandRunner: PLAY");
                    method = StreamServer.PLAY;
                    break;
                case PAUSE:
                    Log.d(App.TAG, "CommandRunner: PAUSE");
                    method = StreamServer.PAUSE;
                    break;
                case SEEK_TO:
                    Log.d(App.TAG, "CommandRunner: SEEK_TO");
                    method = StreamServer.SEEK_TO;
                    break;
                default:
                    return;
            }

            request = new HttpPost("http://" + client.getIP() + ":8888" + method);

            try {
                String response = httpclient.execute(request, responseHandler);

                // Debug
                Log.d(App.TAG, "response: " + response);

            } catch (IOException e) {
                StreamServer.removeClient(client);
//                e.printStackTrace();
            }
        }
    }

}