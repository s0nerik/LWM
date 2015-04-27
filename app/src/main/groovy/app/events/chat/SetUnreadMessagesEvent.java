package app.events.chat;

public class SetUnreadMessagesEvent {

    private int count;

    public SetUnreadMessagesEvent(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
