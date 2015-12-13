package app.data_managers
import app.helper.db.ArtistsCursorGetter
import app.model.Artist
import groovy.transform.CompileStatic
import rx.Observable

@CompileStatic
class ArtistsManager {

    static Observable<Artist> loadAllArtists() {
        CursorConstructor.fromCursorGetter(Artist, new ArtistsCursorGetter())
    }

    static Observable<Artist> loadArtistById(long id) {
        CursorConstructor.fromCursorGetter(Artist, new ArtistsCursorGetter(id))
    }

}
