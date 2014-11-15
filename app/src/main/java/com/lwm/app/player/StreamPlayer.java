package com.lwm.app.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.lwm.app.App;
import com.lwm.app.Injector;
import com.lwm.app.events.client.SendReadyEvent;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.model.Song;
import com.lwm.app.server.StreamServer;
import com.squareup.otto.Bus;

import java.io.IOException;

import javax.inject.Inject;

public class StreamPlayer extends BasePlayer {

    @Inject Context context;

    private static boolean active = false;
    private Song currentSong;

    private Handler handler;

    @Inject Bus bus;

    public static final String STREAM_PATH = StreamServer.Url.STREAM;
    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            Log.d("LWM", "StreamPlayer: onPrepared");
            bus.post(new SendReadyEvent());
        }
    };

    private OnSeekCompleteListener onSeekCompleteListener = new OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            Log.d(App.TAG, "StreamPlayer: onSeekComplete");
            start();
        }
    };

    private OnBufferingUpdateListener onBufferingUpdateListener = new OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
//            Log.d(App.TAG, "Buffered: "+percent);
        }
    };

    public StreamPlayer() {
        super();
        Injector.inject(this);
        handler = new Handler(context.getMainLooper());
        setOnSeekCompleteListener(onSeekCompleteListener);
        setOnPreparedListener(onPreparedListener);
        setOnBufferingUpdateListener(onBufferingUpdateListener);
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
    public boolean isShuffle() {
        // TODO: return shuffle
        return false;
    }

    @Override
    public boolean isRepeat() {
        // TODO: return repeat
        return false;
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        updateSongInfo();
    }

    private void updateSongInfo() {
        Ion.with(context)
                .load(StreamServer.Url.CURRENT_INFO)
                .as(Song.class)
                .withResponse()
                .setCallback(new FutureCallback<Response<Song>>() {
                    @Override
                    public void onCompleted(Exception e, Response<Song> result) {
                        if (e == null) {
                            setCurrentSong(result.getResult());
                            bus.post(new PlaybackStartedEvent(result.getResult(), getCurrentPosition()));
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