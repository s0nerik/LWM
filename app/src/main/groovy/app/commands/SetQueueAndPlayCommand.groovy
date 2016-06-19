package app.commands

import app.models.Song
import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class SetQueueAndPlayCommand {
    List<Song> queue
    int position
    boolean shuffle = false
}