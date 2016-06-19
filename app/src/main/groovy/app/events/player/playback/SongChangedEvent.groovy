package app.events.player.playback;

import app.models.Song
import groovy.transform.CompileStatic;

@CompileStatic
public class SongChangedEvent {

    Song song;

    public SongChangedEvent(Song song) {
        this.song = song;
    }
}
