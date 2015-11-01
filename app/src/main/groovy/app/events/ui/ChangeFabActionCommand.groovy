package app.events.ui

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class ChangeFabActionCommand {
    int iconId
    Closure action
}