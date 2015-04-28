package app.events.player.queue
import app.model.Song
import groovy.transform.CompileStatic

@CompileStatic
public class SongAddedToQueueEvent {

    Song song;
    List<Song> queue;

    public SongAddedToQueueEvent(List<Song> queue, Song song) {
        this.queue = queue;
        this.song = song;
    }
}
