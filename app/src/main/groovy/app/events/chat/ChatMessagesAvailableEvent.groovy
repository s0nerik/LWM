package app.events.chat
import app.model.chat.ChatMessage
import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class ChatMessagesAvailableEvent {
    List<ChatMessage> messages
}
