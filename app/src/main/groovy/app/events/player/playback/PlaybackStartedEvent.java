package app.events.player.playback;

import app.model.Song;

/**
 *
 * Created by sonerik on 7/12/14.
 */
public class PlaybackStartedEvent {

    private int time;
    private Song song;

    public PlaybackStartedEvent(Song song, int time) {
        this.song = song;
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public Song getSong() {
        return song;
    }
}
