package com.lwm.app.events.player;

import com.lwm.app.model.Song;

import java.util.List;

public class PlaylistAddedToQueueEvent {

    private List<Song> queue;

    private List<Song> appendedSongs;

    public PlaylistAddedToQueueEvent(List<Song> queue, List<Song> appendedSongs) {
        this.queue = queue;
        this.appendedSongs = appendedSongs;
    }

    public List<Song> getQueue() {
        return queue;
    }

    public List<Song> getAppendedSongs() {
        return appendedSongs;
    }
}
