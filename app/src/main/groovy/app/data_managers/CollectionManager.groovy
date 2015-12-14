package app.data_managers

import app.model.MusicCollection
import app.model.Song
import groovy.transform.CompileStatic
import rx.Observable

@CompileStatic
class CollectionManager {
    private static MusicCollection collection = new MusicCollection()

    static Observable<Song> init() {
        collection.prepare()
    }
}