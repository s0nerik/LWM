package com.lwm.app.server.async;

import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.SupportAsyncTask;
import com.lwm.app.model.Client;
import com.lwm.app.server.StreamServer;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class CommandRunner extends SupportAsyncTask<CommandRunner.Command, Void, Void> {

    private OkHttpClient httpClient = new OkHttpClient();

    private Request pingRequest;

    private Client client;

    public CommandRunner(Client client) {
        this.client = client;

        pingRequest = new Request.Builder()
                .url("http://" + client.getIP() + ":8888" + StreamServer.PING)
                .post(null)
                .build();
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
            try {
                long start = System.currentTimeMillis();
                Response response = httpClient.newCall(pingRequest).execute();
                long ping = System.currentTimeMillis() - start;
                client.setPing(Math.round(ping/2.));

                response.body().close();

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

            Request request = new Request.Builder()
                    .url("http://" + client.getIP() + ":8888" + method)
                    .post(RequestBody.create(null, ""))
                    .build();

            try {
                httpClient.newCall(request).execute().body().close();

            } catch (IOException e) {
                StreamServer.removeClient(client);
//                e.printStackTrace();
            }
        }
    }

}