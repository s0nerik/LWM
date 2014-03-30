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

public class SwitchClientsSong extends AsyncTask<Integer, Void, Void> {

    public static final int NEXT = 0;
    public static final int PREV = 1;

    HttpClient httpclient = new DefaultHttpClient();
    HttpPost request;
    ResponseHandler<String> responseHandler = new BasicResponseHandler();

    @Override
    protected Void doInBackground(Integer... type) {
        sendRequests(type[0]);
        return null;
    }

    private void sendRequests(int type){
        String method;
        switch(type){
            case NEXT:
                method = StreamServer.NEXT_SONG;
                break;
            case PREV:
                method = StreamServer.PREV_SONG;
                break;
            default:
                return;
        }

        Set<String> clients = StreamServer.getClients();

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

        try {
            while(!clients.equals(StreamServer.getReadyClients())) Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (final String client : clients) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    request = new HttpPost("http://" + client + ":8888" + StreamServer.PLAY);

                    try {
                        String response = httpclient.execute(request, responseHandler);

                        //Debug
                        Log.d(App.TAG, "response: " + response);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        App.getMusicService().getLocalPlayer().start();

    }

}
