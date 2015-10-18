package app.data_managers

import app.model.ArtistWrapper
import groovy.transform.CompileStatic
import rx.Observable

@CompileStatic
class ArtistsManager {

    Observable<ArtistWrapper> loadAllArtists() {
        Observable.just(new ArtistWrapper())
//        Observable.create({
//            CursorConstructor.fromCursorGetter(new ArtistsCursorGetter())
//        } as Observable.OnSubscribe<ArtistWrapper>)
//        Observable.just(new ArtistWrapperList(new ArtistsCursorGetter().getArtistsCursor()).getArtistWrappers())
    }
}
