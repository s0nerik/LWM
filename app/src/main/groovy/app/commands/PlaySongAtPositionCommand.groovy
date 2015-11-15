package app.commands

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class PlaySongAtPositionCommand {
    enum PositionType { NEXT, PREVIOS, EXACT }
    PositionType positionType
    int position = -1
}