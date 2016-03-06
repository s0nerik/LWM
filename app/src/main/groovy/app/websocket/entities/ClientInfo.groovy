package app.websocket.entities

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.apache.commons.lang3.SerializationUtils

@Canonical
@CompileStatic
class ClientInfo implements Serializable {
    String name

    byte[] serialize() {
        SerializationUtils.serialize this
    }

    static ClientInfo deserialize(byte[] bytes) {
        SerializationUtils.deserialize(bytes) as ClientInfo
    }
}
