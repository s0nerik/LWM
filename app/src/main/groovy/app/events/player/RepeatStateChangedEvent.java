package app.events.player;

public class RepeatStateChangedEvent {

    private boolean repeat;

    public boolean isRepeat() {
        return repeat;
    }

    public RepeatStateChangedEvent(boolean repeat) {
        this.repeat = repeat;
    }
}
