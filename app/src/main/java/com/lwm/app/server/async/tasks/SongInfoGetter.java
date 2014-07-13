package com.lwm.app.server.async.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.lwm.app.App;
import com.lwm.app.event.player.PlaybackStartedEvent;
import com.lwm.app.model.Song;
import com.lwm.app.player.StreamPlayer;
import com.lwm.app.server.StreamServer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
* Created by sonerik on 7/13/14.
*/
public class SongInfoGetter extends AsyncTask<Void, Void, Void> {
    private StreamPlayer streamPlayer;
    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpGetPosition = new HttpGet(StreamServer.SERVER_ADDRESS+StreamServer.CURRENT_INFO);
    ResponseHandler<String> responseHandler = new BasicResponseHandler();
    Song song;

    public SongInfoGetter(StreamPlayer streamPlayer) {
        this.streamPlayer = streamPlayer;
    }

    @Override
    protected Void doInBackground(Void... aVoid){

        try {
            String response = httpclient.execute(httpGetPosition, responseHandler);

            //Debug
            Log.d(App.TAG, "response: " + response);

            song = new Gson().fromJson(response, Song.class);
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
