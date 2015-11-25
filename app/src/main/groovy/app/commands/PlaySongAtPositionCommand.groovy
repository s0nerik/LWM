package app.commands

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class PlaySongAtPositionCommand {
    enum PositionType { NEXT, PREVIOUS, EXACT }
    PositionType positionType
    int position = -1
}