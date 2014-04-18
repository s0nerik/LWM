package com.lwm.app.player;

import android.media.MediaPlayer;
import android.util.Log;

import com.lwm.app.App;

public abstract class BasePlayer extends MediaPlayer {

    protected PlayerListener playbackListener;

    public abstract void nextSong();
    public abstract void prevSong();
    public abstract void togglePause();

    public void registerListener(PlayerListener listener){

        Log.d(App.TAG, "BasePlayer: registerListener");

        playbackListener = listener;
    }

    public void unregisterListener(){

        Log.d(App.TAG, "BasePlayer: unregisterListener");

        playbackListener = null;
    }

    public boolean isListenerAttached(){
        return playbackListener != null;
    }

    public String getCurrentDurationInMinutes(){
        int seconds = getDuration()/1000;
        int minutes = seconds/60;
        seconds -= minutes*60;
        return minutes+":"+String.format("%02d",seconds);
    }

    public String getCurrentPositionInMinutes(){
        int seconds = getCurrentPosition()/1000;
        int minutes = seconds/60;
        seconds -= minutes*60;
        return minutes+":"+String.format("%02d",seconds);
    }
}
