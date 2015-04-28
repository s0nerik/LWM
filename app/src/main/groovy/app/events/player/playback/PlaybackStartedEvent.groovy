package app.events.player.playback;

import app.model.Song
import groovy.transform.CompileStatic;

@CompileStatic
public class PlaybackStartedEvent {

    int time;
    Song song;

    public PlaybackStartedEvent(Song song, int time) {
        this.song = song;
        this.time = time;
    }
}
