package com.lwm.app.server.async.tasks;

import android.os.AsyncTask;

import com.lwm.app.player.StreamPlayer;
import com.lwm.app.server.StreamServer;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;

/**
* Created by sonerik on 7/13/14.
*/
public class GetPositionAndStart extends AsyncTask<Void, Void, Void> {
    private StreamPlayer streamPlayer;
    private OkHttpClient httpClient = new OkHttpClient();
    private Request httpGetPosition = new Request.Builder()
            .url(StreamServer.Url.CURRENT_POSITION)
            .get()
            .build();
    int pos;
    long correctionStart, correctionEnd, correction;

    public GetPositionAndStart(StreamPlayer streamPlayer) {
        this.streamPlayer = streamPlayer;
    }

    @Override
    protected Void doInBackground(Void... aVoid){

        try {
            streamPlayer.reset();
            streamPlayer.setDataSource(StreamPlayer.STREAM_PATH);
            streamPlayer.prepare();

            correctionStart = System.currentTimeMillis();
            pos = Integer.parseInt(httpClient.newCall(httpGetPosition).execute().body().string());
            correctionEnd = System.currentTimeMillis();
            correction = correctionEnd - correctionStart;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        streamPlayer.seekTo(pos + (int) (correction / 2.));
        streamPlayer.start();
        new SongInfoGetter(streamPlayer).execute();
    }
}
