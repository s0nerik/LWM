package app.events.player

import app.models.Song
import app.players.BasePlayer
import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class ReadyToStartPlaybackEvent {
    BasePlayer player
    Song song
    int position
}