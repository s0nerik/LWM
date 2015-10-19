package app.data_managers
import android.support.annotation.NonNull
import app.helper.db.SongsCursorGetter
import app.model.Album
import app.model.Song
import groovy.transform.CompileStatic
import rx.Observable
import rx.Subscriber

@CompileStatic
class SongsManager {

    static Observable<Song> loadAllSongs() {
        Observable.create({ Subscriber<Song> subscriber ->
            CursorConstructor.fromCursorGetter(Song, new SongsCursorGetter(), { Song s -> s.source != null })
                    .subscribe subscriber
        } as Observable.OnSubscribe<Song>)
    }

    static Observable<Song> loadAllSongs(@NonNull Album album) {
        Observable.create({ Subscriber<Song> subscriber ->
            CursorConstructor.fromCursorGetter(Song, new SongsCursorGetter(album), { Song s -> s.source != null })
                    .subscribe subscriber
        } as Observable.OnSubscribe<Song>)
    }

}
