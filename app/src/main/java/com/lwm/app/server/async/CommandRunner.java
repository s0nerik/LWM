package com.lwm.app.server.async;

import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.SupportAsyncTask;
import com.lwm.app.model.Client;
import com.lwm.app.server.StreamServer;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.EOFException;
import java.io.IOException;

public class CommandRunner extends SupportAsyncTask<String, Void, Void> {

    private static final int MAX_RETRIES = 3;

    private OkHttpClient httpClient = new OkHttpClient();

    private Request pingRequest;

    private Client client;

    public CommandRunner(Client client) {
        this.client = client;

        pingRequest = new Request.Builder()
                .url("http://" + client.getIP() + ":8888" + StreamServer.Method.PING)
                .post(RequestBody.create(MediaType.parse("text/plain"), ""))
                .build();
    }

    @Override
    protected Void doInBackground(String... methods) {
        for(String method:methods){
            sendRequest(method);
        }
        return null;
    }

    private void sendRequest(String method) {
        sendRequest(method, 0);
    }

    private void sendRequest(String method, int retryCount) {
        if(StreamServer.Method.PING.equals(method)){
            try {
                long start = System.currentTimeMillis();
                System.setProperty("http.keepAlive", "false");
                httpClient.newCall(pingRequest).execute().body().close();
                long ping = System.currentTimeMillis() - start;
                client.setPing(Math.round(ping/2.));

                // Debug
                Log.d(App.TAG, "Ping: "+client.getPing());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Request request = new Request.Builder()
                    .url("http://" + client.getIP() + ":8888" + method)
                    .post(RequestBody.create(MediaType.parse("text/plain"), ""))
                    .build();

            try {
                httpClient.newCall(request).execute().body().close();

            } catch (EOFException e) {
                // TODO: workaround this bug (or use another HttpClient)
                if (retryCount < MAX_RETRIES) {
                    sendRequest(method, retryCount + 1);
                }
            } catch (IOException e) {
                Log.e(App.TAG, "IOException in CommandRunner, method: " + method);
                Log.e(App.TAG, "", e);
                StreamServer.removeClient(client);
            }
        }
    }

}