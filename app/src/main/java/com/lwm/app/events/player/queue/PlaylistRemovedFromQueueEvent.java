package com.lwm.app.events.player.queue;

import com.lwm.app.model.Song;

import java.util.List;

/**
 *
 * Created by sonerik on 7/12/14.
 */
public class PlaylistRemovedFromQueueEvent {

    private List<Song> queue;
    private List<Song> removedSongs;

    public PlaylistRemovedFromQueueEvent(List<Song> queue, List<Song> removedSongs) {
        this.queue = queue;
        this.removedSongs = removedSongs;
    }

    public List<Song> getQueue() {
        return queue;
    }

    public List<Song> getRemovedSongs() {
        return removedSongs;
    }
}
