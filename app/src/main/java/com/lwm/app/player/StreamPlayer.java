package com.lwm.app.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.lib.Connectivity;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class StreamPlayer extends BasePlayer {

    private Context context;

    private static final Uri STREAM_URI = Uri.parse(App.SERVER_ADDRESS+App.STREAM);

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

    public void play(){
        new AddressSender().execute();

        reset();
        try {
            setDataSource(context, STREAM_URI);
            prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setActive();

        Log.d(App.TAG, "StreamPlayer: play()");
    }

    @Override
    public void nextSong() {
        // TODO: this
    }

    @Override
    public void prevSong() {
        // TODO: this
    }

    @Override
    public void togglePause(){
        if (isPlaying()){
            pause();
        }else{
            start();
        }
    }

    private class GetPositionAndStart extends AsyncTask<Void, Void, Void> {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGetPosition = new HttpGet(App.SERVER_ADDRESS+App.CURRENT_POSITION);
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
        }
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