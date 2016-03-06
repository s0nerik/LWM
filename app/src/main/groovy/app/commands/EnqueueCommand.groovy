package app.commands

import app.model.Song
import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class EnqueueCommand {
    List<Song> playlist
}