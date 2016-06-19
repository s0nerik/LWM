package app.events.chat;

import app.models.chat.ChatMessage
import groovy.transform.Canonical
import groovy.transform.CompileStatic;

@Canonical
@CompileStatic
class SendChatMessageEvent {
    ChatMessage message
}
