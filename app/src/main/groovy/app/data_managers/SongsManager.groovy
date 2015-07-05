package app.data_managers

import app.helper.db.Order
import app.helper.db.SongsCursorGetter
import app.model.Playlist
import app.model.Song
import groovy.transform.CompileStatic
import rx.Observable
import rx.subjects.AsyncSubject

@CompileStatic
public class SongsManager {

    private AsyncSubject<List<Song>> songsSubject;

    public Observable<List<Song>> loadAllSongs() {
        if (songsSubject == null) {
            songsSubject = AsyncSubject.create();

            Observable.just(Playlist.fromCursor(new SongsCursorGetter().getSongsCursor(Order.ASCENDING)))
                    .subscribe(songsSubject)
        }
        return songsSubject;
    }

}
