package app.events.p2p

import app.models.Station
import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class StationsListUpdatedEvent {
    List<Station> stations
}