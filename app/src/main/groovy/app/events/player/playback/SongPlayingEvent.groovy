package app.events.player.playback

import groovy.transform.Canonical
import groovy.transform.CompileStatic;

@Canonical
@CompileStatic
public class SongPlayingEvent {
    long progress
    int duration
}
