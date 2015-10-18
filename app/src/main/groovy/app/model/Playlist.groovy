package app.model

import android.database.Cursor
import app.helper.db.SongsCursorGetter
import groovy.transform.CompileStatic
import rx.Observable
import rx.Subscriber

import static app.helper.db.SongsCursorGetter.Column.*

@CompileStatic
class Playlist {

    static Observable<Song> fromCursor(Cursor cursor) {
        Observable.create({ Subscriber<Song> subscriber ->
            if (cursor) {
                if (cursor.moveToFirst()) {
                    def indices = indicesInCursor(cursor)

                    Song song = buildSongFromCursor(cursor, indices)
                    if (song.source) subscriber.onNext song

                    while (cursor.moveToNext()) {
                        song = buildSongFromCursor(cursor, indices)
                        if (song.source) subscriber.onNext song
                    }
                }
                cursor.close()
            }
            subscriber.onCompleted()
        } as Observable.OnSubscribe<Song>)
    }

    private static Song buildSongFromCursor(Cursor cursor, Map<SongsCursorGetter.Column, Integer> indices) {
        Song.builder()
                .songId(cursor.getLong(indices[ID]))
                .artistId(cursor.getLong(indices[ARTIST_ID]))
                .albumId(cursor.getLong(indices[ALBUM_ID]))
                .title(cursor.getString(indices[TITLE]))
                .artist(cursor.getString(indices[ARTIST]))
                .album(cursor.getString(indices[ALBUM]))
                .source(cursor.getString(indices[DATA]))
                .duration(cursor.getInt(indices[DURATION]))
                .build()
    }

}