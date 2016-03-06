package app.websocket

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.apache.commons.lang3.SerializationUtils
import org.java_websocket.WebSocket

@TupleConstructor
@CompileStatic
class SocketMessage implements Serializable {
    static enum Message {
        CURRENT_SONG, CURRENT_POSITION, IS_PLAYING,
        OK, ERROR,
        PAUSE, UNPAUSE, START,
        PREPARE, READY,
        MESSAGE, CLIENT_INFO,
        PING, PONG,
        TIMESTAMP, TIMESTAMP_REQUEST, TIMESTAMP_DIFFERENCE
    }

    static enum Type {
        GET, POST
    }

    Type type
    Message message
    byte[] body

    transient WebSocket socket

    byte[] serialize() {
        SerializationUtils.serialize this
    }

    static SocketMessage deserialize(byte[] bytes) {
        SerializationUtils.deserialize(bytes) as SocketMessage
    }

}