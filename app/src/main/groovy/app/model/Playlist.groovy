package app.model

import android.database.Cursor
import app.helper.db.CursorGetter
import groovy.transform.CompileStatic
import rx.Observable
import rx.Subscriber

import static android.provider.MediaStore.Audio.Media as m

@CompileStatic
class Playlist {

    static Observable<Song> fromCursorGetter(CursorGetter cursorGetter) {
        Observable.create({ Subscriber<Song> subscriber ->
            def cursor = cursorGetter.cursor

            if (cursor) {
                if (cursor.moveToFirst()) {
                    def indices = cursorGetter.projectionIndices()

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

    private static Song buildSongFromCursor(Cursor cursor, Map<String, Integer> indices) {
        Song.builder()
                .songId(cursor.getLong(indices[m._ID]))
                .artistId(cursor.getLong(indices[m.ARTIST_ID]))
                .albumId(cursor.getLong(indices[m.ALBUM_ID]))
                .title(cursor.getString(indices[m.TITLE]))
                .artist(cursor.getString(indices[m.ARTIST]))
                .album(cursor.getString(indices[m.ALBUM]))
                .source(cursor.getString(indices[m.DATA]))
                .duration(cursor.getInt(indices[m.DURATION]))
                .build()
    }

}