package com.lwm.app.model;

import android.media.MediaPlayer;

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

    protected LinkedList<Integer> played;
    protected boolean shuffle;
    protected boolean repeat;
    protected Random generator = new Random();

    public abstract void nextSong();
    public abstract void prevSong();
    public abstract void togglePause();

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
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
