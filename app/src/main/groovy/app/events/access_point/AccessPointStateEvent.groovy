package app.events.access_point

import groovy.transform.CompileStatic;

@CompileStatic
public class AccessPointStateEvent {

    public enum State { CHANGING, DISABLED, ENABLED }

    State state;

    public AccessPointStateEvent(State state) {
        this.state = state;
    }
}
