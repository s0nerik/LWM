package com.lwm.app.player;

import android.media.MediaPlayer;
import android.util.Log;

import com.lwm.app.App;

import java.util.LinkedList;
import java.util.Random;

public abstract class BasePlayer extends MediaPlayer {

    public static final String SONG_CHANGED = "song_changed";
    public static final String PLAYBACK_STARTED = "playback_started";
    public static final String PLAYBACK_PAUSED = "playback_paused";
    public static final String PLAYLIST_POSITION = "playlist_position";
    public static final String CURRENT_POSITION = "current_position";
    public static final String SEEK_POSITION = "seek_position";
    public static final String ALBUM_ART_URI = "album_art_uri";

    protected static LinkedList<Integer> played = new LinkedList<>();

    protected boolean active = false;
    protected static boolean shuffle;
    protected static boolean repeat;
    protected Random generator = new Random();

    protected PlayerListener playbackListener;

    public abstract void nextSong();
    public abstract void prevSong();
    public abstract void togglePause();

    protected void setActive(){
        active = true;
    }

    public boolean isActive(){
        return active;
    }

    public static boolean isShuffle() {
        return shuffle;
    }

    public static void setShuffle(boolean flag) {
        shuffle = flag;
    }

    public static boolean isRepeat() {
        return repeat;
    }

    public static void setRepeat(boolean flag) {
        repeat = flag;
    }

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
