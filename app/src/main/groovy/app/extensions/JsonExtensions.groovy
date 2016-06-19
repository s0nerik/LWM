package app.extensions

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

@CompileStatic
class JsonExtensions {
    public static <T> T fromJson(String json) {
        new JsonSlurper().parseText(json) as T
    }

    public static String toJson(Object obj) {
        JsonOutput.toJson(obj)
    }
}