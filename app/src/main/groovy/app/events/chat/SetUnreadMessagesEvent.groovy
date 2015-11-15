package app.events.chat

import groovy.transform.Canonical
import groovy.transform.CompileStatic;

@Canonical
@CompileStatic
class SetUnreadMessagesEvent {
    int count
}
