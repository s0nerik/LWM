package app.data_managers
import android.support.annotation.NonNull
import app.helper.db.SongsCursorGetter
import app.model.Album
import app.model.Song
import groovy.transform.CompileStatic
import rx.Observable

@CompileStatic
class SongsManager {

    static Observable<Song> loadAllSongs() {
        CursorConstructor.fromCursorGetter(Song, new SongsCursorGetter(), { Song s -> s.source != null })
    }

    static Observable<Song> loadAllSongs(@NonNull Album album) {
        CursorConstructor.fromCursorGetter(Song, new SongsCursorGetter(album), { Song s -> s.source != null })
    }

}
