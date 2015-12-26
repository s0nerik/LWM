package app.data_managers
import android.support.annotation.NonNull
import app.helper.db.AlbumsCursorGetter
import app.model.Album
import app.model.Artist
import groovy.transform.CompileStatic
import rx.Observable

@CompileStatic
class AlbumsManager {

    static Observable<Album> loadAllAlbums(Closure<Boolean> check = { true }) {
        CursorConstructor.fromCursorGetter(Album, new AlbumsCursorGetter(), check)
    }

    static Observable<Album> loadAllAlbums(@NonNull Artist artist, Closure<Boolean> check = { true }) {
        CursorConstructor.fromCursorGetter(Album, new AlbumsCursorGetter(artist), check)
    }

    static Observable<Album> loadAlbumById(long id) {
        CursorConstructor.fromCursorGetter(Album, new AlbumsCursorGetter(id))
    }

}
