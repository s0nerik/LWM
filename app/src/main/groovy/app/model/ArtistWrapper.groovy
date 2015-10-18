package app.model

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@TupleConstructor
@CompileStatic
class ArtistWrapper {
    Artist artist
    List<Album> albums
}