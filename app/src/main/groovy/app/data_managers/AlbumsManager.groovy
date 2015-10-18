package app.data_managers
import android.support.annotation.NonNull
import app.helper.db.AlbumsCursorGetter
import app.model.Album
import app.model.Artist
import groovy.transform.CompileStatic
import rx.Observable
import rx.Subscriber

@CompileStatic
class AlbumsManager {

    static Observable<Album> loadAllAlbums(@NonNull Artist artist) {
        Observable.create({ Subscriber<Album> subscriber ->
            CursorConstructor.fromCursorGetter(Album, new AlbumsCursorGetter(artist))
        } as Observable.OnSubscribe<Album>)
    }

    static Observable<Album> loadAllAlbums() {
        Observable.create({ Subscriber<Album> subscriber ->
            CursorConstructor.fromCursorGetter(Album, new AlbumsCursorGetter())
        } as Observable.OnSubscribe<Album>)
    }

}
