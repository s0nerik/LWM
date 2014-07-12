package com.lwm.app.event.player;

import com.lwm.app.model.Song;

import java.util.List;

/**
 *
 * Created by sonerik on 7/12/14.
 */
public class PlaylistAddedToQueueEvent {

    private List<Song> queue;

    public List<Song> getQueue() {
        return queue;
    }

    public PlaylistAddedToQueueEvent(List<Song> queue) {
        this.queue = queue;
    }
}
