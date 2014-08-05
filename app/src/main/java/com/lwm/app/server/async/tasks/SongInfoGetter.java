package com.lwm.app.server.async.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.lwm.app.App;
import com.lwm.app.event.player.PlaybackStartedEvent;
import com.lwm.app.model.Song;
import com.lwm.app.player.StreamPlayer;
import com.lwm.app.server.StreamServer;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
* Created by sonerik on 7/13/14.
*/
public class SongInfoGetter extends AsyncTask<Void, Void, Void> {
    private StreamPlayer streamPlayer;
    private OkHttpClient httpClient = new OkHttpClient();
    private Request httpGetInfo = new Request.Builder()
            .url(StreamServer.Url.CURRENT_INFO)
            .get()
            .build();

    private Song song;

    public SongInfoGetter(StreamPlayer streamPlayer) {
        this.streamPlayer = streamPlayer;
    }

    @Override
    protected Void doInBackground(Void... aVoid){

        try {
            Response response = httpClient.newCall(httpGetInfo).execute();

            //Debug
            Log.d(App.TAG, "response: " + response);

            song = new Gson().fromJson(response.body().charStream(), Song.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        streamPlayer.setCurrentSong(song);
        App.getEventBus().post(new PlaybackStartedEvent(song, streamPlayer.getCurrentPosition()));
    }
}
