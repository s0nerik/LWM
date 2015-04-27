package app.events.player.playback;

import app.model.Song;

public class SongChangedEvent {

    private Song song;

    public SongChangedEvent(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }
}
