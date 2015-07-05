package app.data_managers

import app.helper.db.ArtistsCursorGetter
import app.model.ArtistWrapper
import app.model.ArtistWrapperList
import groovy.transform.CompileStatic
import rx.Observable
import rx.subjects.AsyncSubject

@CompileStatic
public class ArtistsManager {

    private AsyncSubject<List<ArtistWrapper>> artistsSubject

    public Observable<List<ArtistWrapper>> loadAllArtists() {
        if (artistsSubject == null) {
            artistsSubject = AsyncSubject.create();

            Observable.just(new ArtistWrapperList(new ArtistsCursorGetter().getArtistsCursor()).getArtistWrappers())
                    .subscribe(artistsSubject)
        }
        return artistsSubject;
    }
}
