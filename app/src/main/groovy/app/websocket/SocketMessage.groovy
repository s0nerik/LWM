package app.websocket

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.apache.commons.lang3.SerializationUtils

@TupleConstructor
@CompileStatic
class SocketMessage implements Serializable {
    static enum Message {
        START_FROM, SEEK_TO,
        CURRENT_POSITION, IS_PLAYING,
        OK, ERROR,
        PAUSE, UNPAUSE, START,
        PREPARE, READY,
        MESSAGE, CLIENT_INFO,
        PING, PONG
    }

    static enum Type {
        GET, POST
    }

    Type type
    Message message
    byte[] body

    byte[] serialize() {
        SerializationUtils.serialize this
    }

    static SocketMessage deserialize(byte[] bytes) {
        SerializationUtils.deserialize(bytes) as SocketMessage
    }

}