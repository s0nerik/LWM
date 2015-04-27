package app.events.player.playback.control;

public class ChangeSongEvent {

    public enum Type { NEXT, PREV, TOGGLE_PAUSE, PAUSE, PLAY }

    private Type type;

    public ChangeSongEvent(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
