package app.events.chat
import app.model.chat.ChatMessage
import groovy.transform.CompileStatic

@CompileStatic
public class ChatMessagesAvailableEvent {

    private List<ChatMessage> messages;

    public ChatMessagesAvailableEvent(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }
}
