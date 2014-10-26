package com.lwm.app.events.player.service;

import com.lwm.app.model.Song;

public class CurrentSongAvailableEvent {

    private Song song;

    public CurrentSongAvailableEvent(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }
}
