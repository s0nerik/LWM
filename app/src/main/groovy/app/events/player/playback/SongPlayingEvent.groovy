package app.events.player.playback

import groovy.transform.CompileStatic;

@CompileStatic
public class SongPlayingEvent {

    int progress;
    int duration;

    public SongPlayingEvent(int progress, int duration) {
        this.progress = progress;
        this.duration = duration;
    }
}
