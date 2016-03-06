package app.events.player.queue
import app.model.Song
import groovy.transform.CompileStatic

@CompileStatic
public class PlaylistRemovedFromQueueEvent {

    List<Song> queue;
    List<Song> removedSongs;

    public PlaylistRemovedFromQueueEvent(List<Song> queue, List<Song> removedSongs) {
        this.queue = queue;
        this.removedSongs = removedSongs;
    }
}
