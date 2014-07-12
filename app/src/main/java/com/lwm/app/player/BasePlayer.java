package com.lwm.app.player;

import android.media.MediaPlayer;

public abstract class BasePlayer extends MediaPlayer {

    public abstract void nextSong();
    public abstract void prevSong();
    public abstract void togglePause();

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
