package app.events.server

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@TupleConstructor
@CompileStatic
class MusicServerStateChangedEvent {
    enum State {
        STARTED, STOPPED
    }

    State state
}