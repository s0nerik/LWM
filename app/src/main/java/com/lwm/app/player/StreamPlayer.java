package com.lwm.app.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.lwm.app.App;
import com.lwm.app.events.client.SendReadyEvent;
import com.lwm.app.events.player.PlaybackStartedEvent;
import com.lwm.app.model.Song;
import com.lwm.app.server.StreamServer;

import java.io.IOException;

public class StreamPlayer extends BasePlayer {

    private Context context;
    private static boolean active = false;
    private Song currentSong;

    private Handler handler;

//    private File tempFile;

    public static final String STREAM_PATH = StreamServer.Url.STREAM;
    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            Log.d("LWM", "StreamPlayer: onPrepared");
            App.getBus().post(new SendReadyEvent());
        }
    };

    private OnSeekCompleteListener onSeekCompleteListener = new OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            start();
        }
    };

    public StreamPlayer(Context context) {
        super(context);
        this.context = context;
        handler = new Handler(context.getMainLooper());
        setOnSeekCompleteListener(onSeekCompleteListener);
        setOnPreparedListener(onPreparedListener);
    }

    public void prepareNewSong(){
        reset();
        try {
            setDataSource(STREAM_PATH);
            prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        updateSongInfo();
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        updateSongInfo();
    }

    private void updateSongInfo() {
        Ion.with(App.getContext())
                .load(StreamServer.Url.CURRENT_INFO)
                .as(Song.class)
                .withResponse()
                .setCallback(new FutureCallback<Response<Song>>() {
                    @Override
                    public void onCompleted(Exception e, Response<Song> result) {
                        if (e == null) {
                            setCurrentSong(result.getResult());
                            App.getBus().post(new PlaybackStartedEvent(result.getResult(), getCurrentPosition()));
                        } else {
                            Log.e(App.TAG, "Error getting song info", e);
                        }
                    }
                });
    }


    public static boolean isActive(){
        return active;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }
}