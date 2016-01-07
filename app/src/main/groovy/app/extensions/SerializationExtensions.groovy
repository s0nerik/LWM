package app.extensions

import groovy.transform.CompileStatic
import org.apache.commons.lang3.SerializationUtils

@CompileStatic
class SerializationExtensions {

    static byte[] asByteArray(Serializable obj) {
        return SerializationUtils.serialize(obj)
    }

    static <T> T asObject(byte[] arr) {
        return SerializationUtils.deserialize(arr)
    }

}