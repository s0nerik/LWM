package app.events.player

import app.model.Song
import app.player.BasePlayer
import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class ReadyToStartPlaybackEvent {
    BasePlayer player
    Song song
    int position
}