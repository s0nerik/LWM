package app.events.player;

/**
 *
 * Created by sonerik on 7/12/14.
 */
public class ShuffleStateChangedEvent {

    private boolean shuffle;

    public boolean isShuffle() {
        return shuffle;
    }

    public ShuffleStateChangedEvent(boolean shuffle) {
        this.shuffle = shuffle;
    }

}
