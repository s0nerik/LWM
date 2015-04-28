package app.events.server

import groovy.transform.CompileStatic;

@CompileStatic
public class SeekToClientsEvent {
    int position;

    public SeekToClientsEvent(int position) {
        this.position = position;
    }

}
