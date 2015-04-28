package app.events.player.playback.control

import groovy.transform.CompileStatic;

@CompileStatic
public class ChangeSongEvent {

    public enum Type { NEXT, PREV, TOGGLE_PAUSE, PAUSE, PLAY }

    Type type;

    public ChangeSongEvent(Type type) {
        this.type = type;
    }

}
