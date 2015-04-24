package com.lwm.app.model;

import android.database.Cursor;

import com.lwm.app.helper.db.AlbumsCursorGetter;

import java.util.ArrayList;
import java.util.List;

public class AlbumsList {

    private List<Album> albums = new ArrayList<>();

    public AlbumsList(Cursor cursor) {
        if(cursor.moveToFirst()) {

            int id = AlbumsCursorGetter._ID;
            int album = AlbumsCursorGetter.ALBUM;
            int artist = AlbumsCursorGetter.ARTIST;
            int year = AlbumsCursorGetter.FIRST_YEAR;
            int albumArt = AlbumsCursorGetter.ALBUM_ART;
            int songsCount = AlbumsCursorGetter.NUMBER_OF_SONGS;

            do {
                albums.add(
                        Album.builder()
                                .id(cursor.getInt(id))
                                .title(cursor.getString(album))
                                .artist(cursor.getString(artist))
                                .year(cursor.getInt(year))
                                .albumArtPath(cursor.getString(albumArt))
                                .songsCount(cursor.getInt(songsCount))
                                .build()
                );
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public List<Album> getAlbums() {
        return albums;
    }
}
