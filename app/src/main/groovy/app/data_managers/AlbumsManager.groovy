package app.data_managers

import android.support.annotation.Nullable
import android.support.v4.util.Pair
import app.helper.db.AlbumsCursorGetter
import app.model.Album
import app.model.AlbumsList
import app.model.Artist
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.AsyncSubject

@CompileStatic
public class AlbumsManager {

    @Memoized
    public Observable<Pair<Artist, List<Album>>> loadAllAlbums(@Nullable Artist artist) {
            AlbumsCursorGetter cursorGetter = new AlbumsCursorGetter()

            Observable.just(new Pair<Artist, List<Album>>(artist,
                    new AlbumsList(artist ? cursorGetter.getAlbumsCursorByArtist(artist) : cursorGetter.getAlbumsCursor()).getAlbums()
            ))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

}
