package app.websocket
import app.Injector
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@PackageScope(PackageScopeTarget.FIELDS)
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

    @Inject
    Gson gson

    private init() {
        Injector.inject this
    }

    SocketMessage(Type type, Message message) {
        init()
        this.type = type
        this.message = message
    }

    SocketMessage(Type type, Message message, String body) {
        init()
        this.type = type
        this.message = message
        this.body = body
    }

    String toJson() {
        gson.toJson(this)
    }

    static SocketMessage fromJson(String json) {
        new Gson().fromJson(json, SocketMessage)
    }

}