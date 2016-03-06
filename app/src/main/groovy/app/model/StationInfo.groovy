package app.model

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class StationInfo {
    String port
    String name
    String currentSong
}