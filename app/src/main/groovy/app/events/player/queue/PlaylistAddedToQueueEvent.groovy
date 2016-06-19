package app.events.player.queue
import app.models.Song
import groovy.transform.CompileStatic

@CompileStatic
public class PlaylistAddedToQueueEvent {

    List<Song> queue;

    List<Song> appendedSongs;

    public PlaylistAddedToQueueEvent(List<Song> queue, List<Song> appendedSongs) {
        this.queue = queue;
        this.appendedSongs = appendedSongs;
    }
}
