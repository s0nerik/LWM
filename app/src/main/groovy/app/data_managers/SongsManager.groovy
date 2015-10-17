package app.data_managers

import android.database.Cursor
import app.helper.db.Order
import app.helper.db.SongsCursorGetter
import app.model.Album
import app.model.Playlist
import app.model.Song
import groovy.transform.CompileStatic
import rx.Observable
import rx.Subscriber

@CompileStatic
class SongsManager {

    Observable<Song> loadAllSongs() {
        Observable.create({ Subscriber<Song> subscriber ->
            new SongsCursorGetter().getSongsCursor(Order.ASCENDING).subscribe { Cursor it ->
                Playlist.fromCursor(it).subscribe subscriber
            }
        } as Observable.OnSubscribe<Song>)
    }

    Observable<Song> loadSongsForAlbum(Album album) {
        Observable.create({ Subscriber<Song> subscriber ->
            new SongsCursorGetter().getSongsCursor(Order.ASCENDING, album).subscribe { Cursor it ->
                Playlist.fromCursor(it).subscribe subscriber
            }
        } as Observable.OnSubscribe<Song>)
    }

}
