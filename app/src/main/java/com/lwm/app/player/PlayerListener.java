package com.lwm.app.player;

import com.lwm.app.model.Song;

public interface PlayerListener {
    public void onSongChanged(Song song);
//    public void onPausePlayback();
//    public void onPlay();
}
