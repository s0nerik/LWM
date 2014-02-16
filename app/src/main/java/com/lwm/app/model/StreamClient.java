package com.lwm.app.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.lib.Connectivity;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class StreamClient {

    public static final String SONG_CHANGED = "song_changed";
    public static final String PLAYLIST_POSITION = "playlist_position";
    public static final String CURRENT_POSITION = "current_position";

    private Context context;
    private StreamPlayer player;

    public StreamClient(Context context){
        this.context = context;
    }

    public void connect(){
        new AddressSender().execute();
    }

    private class AddressSender extends AsyncTask<Void, Void, Void> {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPostIP = new HttpPost(App.SERVER_ADDRESS+"/ip/"+ Connectivity.getIP(context));

        @Override
        protected Void doInBackground(Void... aVoid){
            try {
                httpclient.execute(httpPostIP);
            } catch (IOException e) {
                Log.e(App.TAG, "Error: AddressSender");
                e.printStackTrace();
            }

            return null;
        }
    }

}