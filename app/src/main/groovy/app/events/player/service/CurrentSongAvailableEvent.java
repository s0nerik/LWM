package app.events.player.service;

import app.model.Song;

public class CurrentSongAvailableEvent {

    private Song song;

    public CurrentSongAvailableEvent(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }
}
