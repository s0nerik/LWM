package app.model

import groovy.transform.CompileStatic

@CompileStatic
class Playlist {

//    static Observable<Song> fromCursorGetter(CursorGetter cursorGetter) {
//        Observable.create({ Subscriber<Song> subscriber ->
//            def cursor = cursorGetter.cursor
//
//            if (cursor) {
//                if (cursor.moveToFirst()) {
//                    def indices = cursorGetter.projectionIndices()
//
//                    Song song = Song.initialize(cursor, indices)
//                    if (song.source) subscriber.onNext song
//
//                    while (cursor.moveToNext()) {
//                        song = Song.initialize(cursor, indices)
//                        if (song.source) subscriber.onNext song
//                    }
//                }
//                cursor.close()
//            }
//            subscriber.onCompleted()
//        } as Observable.OnSubscribe<Song>)
//    }

}