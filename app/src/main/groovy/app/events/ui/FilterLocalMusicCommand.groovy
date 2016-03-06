package app.events.ui

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@TupleConstructor
@CompileStatic
class FilterLocalMusicCommand {
    CharSequence constraint
}