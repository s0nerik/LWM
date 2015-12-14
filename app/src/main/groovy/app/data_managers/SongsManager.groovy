package app.data_managers
import android.support.annotation.NonNull
import app.helper.db.SongsCursorGetter
import app.model.Album
import app.model.Song
import groovy.transform.CompileStatic
import rx.Observable

@CompileStatic
class SongsManager {

    private static final Observable<Song> songs =
            CursorConstructor.fromCursorGetter(Song, new SongsCursorGetter(), { Song s -> (s.source != null) as boolean })
                             .cache()

    static Observable<Song> loadAllSongs() {
        songs
    }

    static Observable<Song> loadAllSongs(@NonNull Album album) {
        songs.filter { it.albumId == album.id }
//        CursorConstructor.fromCursorGetter(Song, new SongsCursorGetter(album), { Song s -> s.source != null })
    }

}
