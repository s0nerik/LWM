package app.events.server;

public class SeekToClientsEvent {
    private int position;

    public SeekToClientsEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

}
