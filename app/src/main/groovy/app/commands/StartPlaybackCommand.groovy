package app.commands

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class StartPlaybackCommand {
    long startAt = System.currentTimeMillis()
}