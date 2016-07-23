package app.events.player.playback

import groovy.transform.Canonical
import groovy.transform.CompileStatic;

@Canonical
@CompileStatic
class SongPlayingEvent {
    long progress
    int duration

    float getProgressPercent() {
        return progress * 100f / duration
    }
}
