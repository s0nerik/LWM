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

    Observable<Song> loadAllSongs() {
        Observable.create({ Subscriber<Song> subscriber ->
            CursorConstructor.fromCursorGetter(Song, new SongsCursorGetter()).subscribe subscriber
        } as Observable.OnSubscribe<Song>)
    }

    Observable<Song> loadAllSongs(@NonNull Album album) {
        Observable.create({ Subscriber<Song> subscriber ->
            CursorConstructor.fromCursorGetter(Song, new SongsCursorGetter(album)).subscribe subscriber
        } as Observable.OnSubscribe<Song>)
    }

}
