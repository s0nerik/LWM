package app.model

import android.database.Cursor

import app.helper.db.SongsCursorGetter
import groovy.transform.CompileStatic
import rx.Observable
import rx.Subscriber

@CompileStatic
class Playlist {

    static Observable<Song> fromCursor(Cursor cursor) {
        Observable.create({ Subscriber<Song> subscriber ->
            if (cursor) {
                if (cursor.moveToFirst()) {
                    subscriber.onNext buildSongFromCursor(cursor)
                    while (cursor.moveToNext()) {
                        subscriber.onNext buildSongFromCursor(cursor)
                    }
                }
                cursor.close()
            }
            subscriber.onCompleted()
        } as Observable.OnSubscribe<Song>)
    }

    private static Song buildSongFromCursor(Cursor cursor) {
        Song.builder()
                .songId(cursor.getLong(SongsCursorGetter._ID))
                .artistId(cursor.getLong(SongsCursorGetter.ARTIST_ID))
                .albumId(cursor.getLong(SongsCursorGetter.ALBUM_ID))
                .title(cursor.getString(SongsCursorGetter.TITLE))
                .artist(cursor.getString(SongsCursorGetter.ARTIST))
                .album(cursor.getString(SongsCursorGetter.ALBUM))
                .source(cursor.getString(SongsCursorGetter.DATA))
                .duration(cursor.getInt(SongsCursorGetter.DURATION))
                .build()
    }

}