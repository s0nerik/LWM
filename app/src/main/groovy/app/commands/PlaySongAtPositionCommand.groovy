package app.commands

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class PlaySongAtPositionCommand {
    int position = 0
}