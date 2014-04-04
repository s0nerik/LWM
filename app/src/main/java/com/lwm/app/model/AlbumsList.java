package com.lwm.app.model;

import android.database.Cursor;

import com.lwm.app.helper.AlbumsCursorGetter;

import java.util.ArrayList;
import java.util.List;

public class AlbumsList {

    private List<Album> albums = new ArrayList<>();

    public AlbumsList(Cursor cursor) {
        cursor.moveToFirst();

//        int id = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
//        int album = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
//        int artist = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
//        int year = cursor.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR);
//        int albumArt = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
//        int songsCount = cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);

        int id = AlbumsCursorGetter._ID;
        int album = AlbumsCursorGetter.ALBUM;
        int artist = AlbumsCursorGetter.ARTIST;
        int year = AlbumsCursorGetter.FIRST_YEAR;
        int albumArt = AlbumsCursorGetter.ALBUM_ART;
        int songsCount = AlbumsCursorGetter.NUMBER_OF_SONGS;

        do{
            albums.add(new Album(
                    cursor.getInt(id),
                    cursor.getString(album),
                    cursor.getString(artist),
                    cursor.getInt(year),
                    cursor.getString(albumArt),
                    cursor.getInt(songsCount)
            ));
        }while(cursor.moveToNext());
        cursor.close();
    }

    public List<Album> getAlbums() {
        return albums;
    }
}
