package app.websocket
import com.google.gson.annotations.Expose
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

    @Expose
    Type type

    @Expose
    Message message

    @Expose
    String body

    byte[] serialize() {
        SerializationUtils.serialize this
//        asByteArray()
    }

    static SocketMessage deserialize(byte[] bytes) {
        SerializationUtils.deserialize(bytes) as SocketMessage
//        bytes.<SocketMessage> asObject()
    }

}