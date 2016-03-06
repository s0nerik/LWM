package app.commands

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class StartPlaybackDelayedCommand {
    long startAt = System.currentTimeMillis()
}