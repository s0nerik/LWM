package app.data_managers
import app.helper.db.Order
import app.helper.db.SongsCursorGetter
import app.model.Playlist
import app.model.Song
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

@CompileStatic
public class SongsManager {

//    private AsyncSubject<List<Song>> songsSubject;

    @Memoized
    public Observable<List<Song>> loadAllSongs() {
//        if (!songsSubject) {
//            songsSubject = AsyncSubject.create()

            Observable.just(Playlist.fromCursor(new SongsCursorGetter().getSongsCursor(Order.ASCENDING)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(songsSubject)
//        }
//        return songsSubject
    }

}
