package app.models.chat

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.apache.commons.lang3.SerializationUtils

@Canonical
@CompileStatic
class ChatMessage implements Serializable {
    String message
    String author

    byte[] serialize() {
        SerializationUtils.serialize this
    }

    static ChatMessage deserialize(byte[] bytes) {
        SerializationUtils.deserialize(bytes) as ChatMessage
    }
}
