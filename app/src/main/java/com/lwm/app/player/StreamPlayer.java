package com.lwm.app.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.lwm.app.App;
import com.lwm.app.lib.Connectivity;
import com.lwm.app.model.Song;
import com.lwm.app.server.StreamServer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class StreamPlayer extends BasePlayer {

    private Context context;
    private static boolean active = false;

    private static final Uri STREAM_URI = Uri.parse(StreamServer.SERVER_ADDRESS+ StreamServer.STREAM);

    public static final String SONG_CHANGED = "song_changed";
    public static final String PLAYLIST_POSITION = "playlist_position";
    public static final String CURRENT_POSITION = "current_position";

    public StreamPlayer(Context context){
        this.context = context;
        initOnPreparedListener();
    }

    private void initOnPreparedListener(){
        setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d("LWM", "StreamPlayer: onPrepared");
                new GetPositionAndStart().execute();
            }
        });
    }

    public void playFromCurrentPosition(){
        new AddressSender().execute();

        reset();
        try {
            setDataSource(context, STREAM_URI);
            prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        active = true;

        Log.d(App.TAG, "StreamPlayer: playFromCurrentPosition()");
    }

    @Override
    public void nextSong() {
        Log.d(App.TAG, "StreamPlayer: nextSong");

        reset();
        try {
            setDataSource(context, STREAM_URI);
            prepare();
            new ReadinessReporter().execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void prevSong() {
        Log.d(App.TAG, "StreamPlayer: prevSong");

        reset();
        try {
            setDataSource(context, STREAM_URI);
            prepare();
            new ReadinessReporter().execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void togglePause(){
        if (isPlaying()){
            pause();
        }else{
            start();
        }
    }

    public static boolean isActive(){
        return active;
    }

    private class GetPositionAndStart extends AsyncTask<Void, Void, Void> {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGetPosition = new HttpGet(StreamServer.SERVER_ADDRESS+ StreamServer.CURRENT_POSITION);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        int pos;
        long correctionStart, correctionEnd, correction;

        @Override
        protected Void doInBackground(Void... aVoid){

            try {
                correctionStart = System.currentTimeMillis();
                pos = Integer.parseInt(httpclient.execute(httpGetPosition, responseHandler));
                correctionEnd = System.currentTimeMillis();
                correction = correctionEnd - correctionStart;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            seekTo(pos+(int)correction);
            start();
            new SongInfoGetter().execute();
        }
    }

    private class SongInfoGetter extends AsyncTask<Void, Void, Void> {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGetPosition = new HttpGet(StreamServer.SERVER_ADDRESS+ StreamServer.CURRENT_INFO);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        Song song;

        @Override
        protected Void doInBackground(Void... aVoid){

            try {
                String response = httpclient.execute(httpGetPosition, responseHandler);

                //Debug
                Log.d(App.TAG, "response: "+response);

                song = new Gson().fromJson(response, Song.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            playbackListener.onSongChanged(song);
        }
    }

    private class AddressSender extends AsyncTask<Void, Void, Void> {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPostIP = new HttpPost(StreamServer.SERVER_ADDRESS+"/ip/"+ Connectivity.getIP(context));

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

    private class ReadinessReporter extends AsyncTask<Void, Void, Void> {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPostReady = new HttpPost(StreamServer.SERVER_ADDRESS+StreamServer.CLIENT_READY+Connectivity.getIP(context));

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

    }

}