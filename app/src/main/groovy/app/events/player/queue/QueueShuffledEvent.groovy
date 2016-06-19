package app.events.player.queue
import app.models.Song
import groovy.transform.CompileStatic

@CompileStatic
public class QueueShuffledEvent {

    List<Song> queue;

    public List<Song> getQueue() {
        return queue;
    }
}
