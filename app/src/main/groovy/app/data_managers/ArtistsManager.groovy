package app.data_managers

import app.helper.db.ArtistsCursorGetter
import app.model.Artist
import app.model.ArtistWrapper
import groovy.transform.CompileStatic
import rx.Observable

@CompileStatic
class ArtistsManager {

    Observable<ArtistWrapper> loadAllArtists() {
        Observable.create({

            CursorConstructor.fromCursorGetter(Artist, new ArtistsCursorGetter())
                    .map { new ArtistWrapper(it, []) }

        } as Observable.OnSubscribe<ArtistWrapper>)
    }

}
