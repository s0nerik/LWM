package com.lwm.app.events.player.playback;

import com.lwm.app.model.Song;

public class SongChangedEvent {

    private Song song;

    public SongChangedEvent(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }
}
