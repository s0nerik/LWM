package com.lwm.app.server.async.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.player.StreamPlayer;
import com.lwm.app.server.StreamServer;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class ReadinessReporter extends AsyncTask<Void, Void, Void> {
    private OkHttpClient httpClient = new OkHttpClient();
    private Request httpPostReady = new Request.Builder()
                                        .url(StreamServer.Url.CLIENT_READY)
                                        .post(RequestBody.create(null, ""))
                                        .build();
    private StreamPlayer player;

    public ReadinessReporter(StreamPlayer player) {
        this.player = player;
    }

    @Override
    protected Void doInBackground(Void... aVoid){

        try {
            Response response = httpClient.newCall(httpPostReady).execute();
            response.body().close();
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
