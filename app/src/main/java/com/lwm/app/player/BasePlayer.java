package com.lwm.app.player;

import android.media.MediaPlayer;
import android.util.Log;

import com.lwm.app.App;

import java.util.HashSet;

public abstract class BasePlayer extends MediaPlayer {

    protected HashSet<PlayerListener> playbackListeners = new HashSet<>();

    public abstract void nextSong();
    public abstract void prevSong();
    public abstract void togglePause();

    public void registerListener(PlayerListener listener){

        Log.d(App.TAG, "BasePlayer: registerListener");

        playbackListeners.add(listener);
    }

    public void unregisterListener(PlayerListener listener){

        Log.d(App.TAG, "BasePlayer: unregisterListener");

        playbackListeners.remove(listener);
    }

    public boolean hasListeners(){
        return !playbackListeners.isEmpty();
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
