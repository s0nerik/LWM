package com.lwm.app.server.async.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.player.StreamPlayer;
import com.lwm.app.server.StreamServer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class ReadinessReporter extends AsyncTask<Void, Void, Void> {
    private HttpClient httpclient = new DefaultHttpClient();
    private HttpPost httpPostReady = new HttpPost(StreamServer.SERVER_ADDRESS+StreamServer.CLIENT_READY);
    private StreamPlayer player;

    public ReadinessReporter(StreamPlayer player) {
        this.player = player;
    }

    @Override
    protected Void doInBackground(Void... aVoid){

        try {
            httpclient.execute(httpPostReady);
        } catch (IOException e) {
            Log.e(App.TAG, "Error: ReadinessReporter");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        new SongInfoGetter(player).execute();
    }
}
