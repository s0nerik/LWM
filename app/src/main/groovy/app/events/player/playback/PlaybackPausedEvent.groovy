package app.events.player.playback;

import app.model.Song
import groovy.transform.CompileStatic;

@CompileStatic
public class PlaybackPausedEvent {

    int time;
    Song song;

    public PlaybackPausedEvent(Song song, int time) {
        this.song = song;
        this.time = time;
    }

}
