package app.data_managers
import android.support.annotation.NonNull
import app.helper.db.AlbumsCursorGetter
import app.model.Album
import app.model.Artist
import groovy.transform.CompileStatic
import rx.Observable

@CompileStatic
class AlbumsManager {

    static Observable<Album> loadAllAlbums() {
        CursorConstructor.fromCursorGetter(Album, new AlbumsCursorGetter())
    }

    static Observable<Album> loadAllAlbums(@NonNull Artist artist) {
        CursorConstructor.fromCursorGetter(Album, new AlbumsCursorGetter(artist))
    }

    static Observable<Album> loadAlbumById(long id) {
        CursorConstructor.fromCursorGetter(Album, new AlbumsCursorGetter(id))
    }

}
