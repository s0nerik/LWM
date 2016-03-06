package app.events.player

import groovy.transform.CompileStatic;

@CompileStatic
public class RepeatStateChangedEvent {

    boolean repeat;

    public RepeatStateChangedEvent(boolean repeat) {
        this.repeat = repeat;
    }
}
