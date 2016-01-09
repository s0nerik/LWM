package app.websocket.entities

import app.model.Song
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.apache.commons.lang3.SerializationUtils

@Canonical
@CompileStatic
class PrepareInfo implements Serializable {
    Song song
    long serverTime = System.currentTimeMillis()
    int position = 0
    boolean seeking = false
    boolean autostart = false

    byte[] serialize() {
        SerializationUtils.serialize this
    }

    static PrepareInfo deserialize(byte[] bytes) {
        SerializationUtils.deserialize(bytes) as PrepareInfo
    }
}