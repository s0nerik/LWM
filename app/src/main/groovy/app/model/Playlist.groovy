package app.model

import android.database.Cursor

import app.helper.db.SongsCursorGetter
import groovy.transform.CompileStatic

@CompileStatic
public class Playlist {

    public static List<Song> fromCursor(Cursor cursor) {
        List<Song> songs = new ArrayList<>()
        if (cursor) {
            if (cursor.moveToFirst()) {
                songs << buildSongFromCursor(cursor)
                while (cursor.moveToNext()) {
                    songs << buildSongFromCursor(cursor)
                }
            }
            cursor.close()
        }
        return songs
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