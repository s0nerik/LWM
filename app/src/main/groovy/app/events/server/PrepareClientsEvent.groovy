package app.events.server

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class PrepareClientsEvent {
    int position
}
