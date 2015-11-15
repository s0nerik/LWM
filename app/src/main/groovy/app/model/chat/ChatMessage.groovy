package app.model.chat

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class ChatMessage {
    String message
    String author
}
