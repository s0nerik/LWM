package app.events.chat

import groovy.transform.CompileStatic;

@CompileStatic
public class SetUnreadMessagesEvent {

    int count;

    public SetUnreadMessagesEvent(int count) {
        this.count = count;
    }
}
