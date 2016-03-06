package app.events.player.playback.control

import groovy.transform.Canonical
import groovy.transform.CompileStatic;

@Canonical
@CompileStatic
class ControlButtonEvent {

    enum Type { NEXT, PREV, TOGGLE_PAUSE, PAUSE, PLAY }

    Type type

}
