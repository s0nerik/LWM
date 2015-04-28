package app.events.player

import groovy.transform.CompileStatic;

@CompileStatic
public class ShuffleStateChangedEvent {

    boolean shuffle;

    public ShuffleStateChangedEvent(boolean shuffle) {
        this.shuffle = shuffle;
    }

}
