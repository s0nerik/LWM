package app.data_managers

import app.helper.db.ArtistsCursorGetter
import app.model.ArtistWrapper
import app.model.ArtistWrapperList
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.AsyncSubject

@CompileStatic
public class ArtistsManager {

    private AsyncSubject<List<ArtistWrapper>> artistsSubject

    @Memoized
    public Observable<List<ArtistWrapper>> loadAllArtists() {
        if (artistsSubject == null) {
            artistsSubject = AsyncSubject.create();

            Observable.just(new ArtistWrapperList(new ArtistsCursorGetter().getArtistsCursor()).getArtistWrappers())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(artistsSubject)
        }
        return artistsSubject
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

//    public Observable<List<ArtistWrapper>> loadAllArtists() {
//            Observable.just(new ArtistWrapperList(new ArtistsCursorGetter().getArtistsCursor()).getArtistWrappers())
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//    }
}
