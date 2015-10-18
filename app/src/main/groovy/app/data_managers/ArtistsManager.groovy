package app.data_managers
import app.helper.db.ArtistsCursorGetter
import app.model.Artist
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import rx.Observable
import rx.Subscriber

@CompileStatic
class ArtistsManager {

    @Memoized
    static Observable<Artist> loadAllArtists() {
        Observable.create({ Subscriber<Artist> subscriber ->
            CursorConstructor.fromCursorGetter(Artist, new ArtistsCursorGetter()).subscribe subscriber
        } as Observable.OnSubscribe<Artist>).cache()
    }

    @Memoized
    static Observable<Artist> loadArtistById(long id) {
        Observable.create({ Subscriber<Artist> subscriber ->
            CursorConstructor.fromCursorGetter(Artist, new ArtistsCursorGetter(id)).subscribe subscriber
        } as Observable.OnSubscribe<Artist>).cache()
    }

}
