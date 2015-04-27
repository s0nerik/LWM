package app.events.player.queue;

import app.model.Song;

import java.util.List;

/**
 *
 * Created by sonerik on 7/12/14.
 */
public class SongAddedToQueueEvent {

    private Song song;
    private List<Song> queue;

    public SongAddedToQueueEvent(List<Song> queue, Song song) {
        this.queue = queue;
        this.song = song;
    }

    public Song getSong() {
        return song;
    }

    public List<Song> getQueue() {
        return queue;
    }
}
