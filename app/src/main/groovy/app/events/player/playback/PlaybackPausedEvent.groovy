package app.events.player.playback;

import app.models.Song
import groovy.transform.Canonical
import groovy.transform.CompileStatic;

@Canonical
@CompileStatic
class PlaybackPausedEvent {
    Song song
    long time
}
