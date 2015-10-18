package app.data_managers

import app.helper.db.ArtistsCursorGetter
import app.model.Artist
import app.model.ArtistWrapper
import groovy.transform.CompileStatic
import rx.Observable
import rx.Subscriber

@CompileStatic
class ArtistsManager {

    Observable<ArtistWrapper> loadAllArtists() {
        Observable.create({ Subscriber<ArtistWrapper> subscriber ->
            CursorConstructor.fromCursorGetter(Artist, new ArtistsCursorGetter())
                    .map {
                        new ArtistWrapper(it, AlbumsManager.loadAllAlbums(it).toList().toBlocking().first())
                    }
        } as Observable.OnSubscribe<ArtistWrapper>)
    }

}
