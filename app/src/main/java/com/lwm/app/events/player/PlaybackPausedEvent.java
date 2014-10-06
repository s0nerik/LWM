package com.lwm.app.events.player;

import com.lwm.app.model.Song;

/**
 *
 * Created by sonerik on 7/12/14.
 */
public class PlaybackPausedEvent {

    private int time;
    private Song song;

    public PlaybackPausedEvent(Song song, int time) {
        this.song = song;
        this.time = time;
    }

}
