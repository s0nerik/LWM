package com.lwm.app.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.event.player.PlaybackPausedEvent;
import com.lwm.app.event.player.PlaybackStartedEvent;
import com.lwm.app.model.Song;
import com.lwm.app.server.StreamServer;
import com.lwm.app.server.async.tasks.ClientRegister;
import com.lwm.app.server.async.tasks.ClientUnregister;
import com.lwm.app.server.async.tasks.GetPositionAndStart;
import com.lwm.app.server.async.tasks.ReadinessReporter;

import java.io.IOException;

public class StreamPlayer extends BasePlayer {

    private Context context;
    private static boolean active = false;
    private Song currentSong;

    public static final String STREAM_PATH = StreamServer.Url.STREAM;

    public StreamPlayer(Context context){
        this.context = context;
        attachToStation();
        setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d("LWM", "StreamPlayer: onPrepared");
                new ReadinessReporter(StreamPlayer.this).execute();
            }
        });
    }

    public void attachToStation(){
        new ClientRegister().execute();
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

    public void playFromCurrentPosition(){

        new GetPositionAndStart(this).execute();

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
        App.getEventBus().post(new PlaybackPausedEvent(currentSong, getCurrentPosition()));
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        App.getEventBus().post(new PlaybackStartedEvent(currentSong, getCurrentPosition()));
    }

    public void detachFromStation(){
        new ClientUnregister().execute();
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