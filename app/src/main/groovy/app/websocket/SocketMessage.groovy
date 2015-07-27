package app.websocket
import app.Utils
import com.google.gson.annotations.Expose
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@TupleConstructor
@CompileStatic
class SocketMessage {
    static enum Message {
        START_FROM, SEEK_TO,
        CURRENT_POSITION, IS_PLAYING,
        OK, ERROR,
        PAUSE, UNPAUSE, START,
        PREPARE, READY,
        MESSAGE, CLIENT_INFO
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

    String toJson() {
        Utils.toJson this
//        gson.toJson(this)
    }

}