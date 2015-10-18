package app.model

import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
@Parcelable(exclude = {metaClass})
class ArtistWrapper {
    Artist artist
    List<Album> albums
}