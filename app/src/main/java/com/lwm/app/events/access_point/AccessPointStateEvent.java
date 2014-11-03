package com.lwm.app.events.access_point;

public class AccessPointStateEvent {

    public enum State { CHANGING, DISABLED, ENABLED }

    private State state;

    public AccessPointStateEvent(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }
}
