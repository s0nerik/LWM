package com.lwm.app.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.lwm.app.App;
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
    private static Song currentSong;

    private static final Uri STREAM_URI = Uri.parse(StreamServer.SERVER_ADDRESS+StreamServer.STREAM);

    public StreamPlayer(Context context){
        this.context = context;
        attachToStation();
        setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d("LWM", "StreamPlayer: onPrepared");
                new ReadinessReporter().execute();
            }
        });
    }

    public void attachToStation(){
        new ClientRegister().execute();
    }

    public void prepareNewSong(){
        reset();
        try {
            setDataSource(context, STREAM_URI);
            prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playFromCurrentPosition(){

        new GetPositionAndStart().execute();

        active = true;

        Log.d(App.TAG, "StreamPlayer: playFromCurrentPosition()");
    }

    @Override
    public void nextSong() {
        Log.d(App.TAG, "StreamPlayer: nextSong");
        prepareNewSong();
    }

    @Override
    public void prevSong() {
        Log.d(App.TAG, "StreamPlayer: prevSong");
        prepareNewSong();
    }

    @Override
    public void togglePause(){
        if (isPlaying()){
            pause();
        }else{
            start();
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        if(hasListeners()) {
            for(PlayerListener listener:playbackListeners) {
                listener.onPlaybackPaused();
            }
        }
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        if(hasListeners()) {
            for(PlayerListener listener:playbackListeners) {
                listener.onPlaybackStarted();
            }
        }
    }

    public void detachFromStation(){
        new ClientUnregister().execute();
    }

    public static boolean isActive(){
        return active;
    }

    public static Song getCurrentSong() {
        return currentSong;
    }

    private class GetPositionAndStart extends AsyncTask<Void, Void, Void> {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGetPosition = new HttpGet(StreamServer.SERVER_ADDRESS+StreamServer.CURRENT_POSITION);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        int pos;
        long correctionStart, correctionEnd, correction;

        @Override
        protected Void doInBackground(Void... aVoid){

            try {
                reset();
                setDataSource(context, STREAM_URI);
                prepare();

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
        HttpGet httpGetPosition = new HttpGet(StreamServer.SERVER_ADDRESS+StreamServer.CURRENT_INFO);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        Song song;

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
            currentSong = song;
            if(hasListeners()) {
                for (PlayerListener listener : playbackListeners) {
                    listener.onSongChanged(song);
                }
            }
        }
    }

    private class ClientRegister extends AsyncTask<Void, Void, Void> {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPostRegister = new HttpPost(StreamServer.SERVER_ADDRESS+StreamServer.CLIENT_REGISTER);

        @Override
        protected Void doInBackground(Void... aVoid){

            try {
                httpclient.execute(httpPostRegister);
            } catch (IOException e) {
                Log.e(App.TAG, "Error: ClientRegister");
                e.printStackTrace();
            }

            return null;
        }

    }

    private class ClientUnregister extends AsyncTask<Void, Void, Void> {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPostUnregister = new HttpPost(StreamServer.SERVER_ADDRESS+StreamServer.CLIENT_UNREGISTER);

        @Override
        protected Void doInBackground(Void... aVoid){

            try {
                httpclient.execute(httpPostUnregister);
            } catch (IOException e) {
                Log.e(App.TAG, "Error: ClientUnregister");
                e.printStackTrace();
            }

            return null;
        }

    }

    private class ReadinessReporter extends AsyncTask<Void, Void, Void> {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPostReady = new HttpPost(StreamServer.SERVER_ADDRESS+StreamServer.CLIENT_READY);

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
            new SongInfoGetter().execute();
        }
    }

}