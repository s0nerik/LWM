package com.lwm.app.events.player;

import com.lwm.app.model.Song;

import java.util.List;

/**
 *
 * Created by sonerik on 7/12/14.
 */
public class QueueShuffledEvent {

    private List<Song> queue;

    public List<Song> getQueue() {
        return queue;
    }

    public QueueShuffledEvent(List<Song> queue) {
        this.queue = queue;
    }
}
