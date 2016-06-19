package app.events.player.queue
import app.models.Song
import groovy.transform.CompileStatic

@CompileStatic
public class SongRemovedFromQueueEvent {

    Song song;
    List<Song> queue;

    public SongRemovedFromQueueEvent(List<Song> queue, Song song) {
        this.queue = queue;
        this.song = song;
    }
}
