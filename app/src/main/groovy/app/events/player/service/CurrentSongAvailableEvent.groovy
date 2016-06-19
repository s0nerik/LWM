package app.events.player.service;

import app.models.Song;
import groovy.transform.CompileStatic;

@CompileStatic
public class CurrentSongAvailableEvent {

    Song song;

    public CurrentSongAvailableEvent(Song song) {
        this.song = song;
    }
}
