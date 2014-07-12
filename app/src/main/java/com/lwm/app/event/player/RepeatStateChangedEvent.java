package com.lwm.app.event.player;

/**
 *
 * Created by sonerik on 7/12/14.
 */
public class RepeatStateChangedEvent {

    private boolean repeat;

    public boolean isRepeat() {
        return repeat;
    }

    public RepeatStateChangedEvent(boolean repeat) {
        this.repeat = repeat;
    }
}
