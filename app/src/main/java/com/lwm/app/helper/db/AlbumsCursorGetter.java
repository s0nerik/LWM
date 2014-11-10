package com.lwm.app.helper.db;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import com.lwm.app.Injector;
import com.lwm.app.model.Album;
import com.lwm.app.model.Artist;

import javax.inject.Inject;

public class AlbumsCursorGetter {

    private String artist;

    @Inject
    ContentResolver contentResolver;

    private final String[] projection = {
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.FIRST_YEAR,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
//            MediaStore.Audio.Albums.ALBUM_KEY
//            MediaStore.Audio.AudioColumns.ALBUM_ID
    };

    private String selection = null;
    private String[] selectionArgs = null;

    public static final int _ID             = 0;
    public static final int ALBUM           = 1;
    public static final int ALBUM_ART       = 2;
    public static final int ARTIST          = 3;
    public static final int FIRST_YEAR      = 4;
    public static final int NUMBER_OF_SONGS = 5;
//    public static final int ALBUM_KEY    = 4;
//    public static final int ALBUM_ID     = 4;

    public AlbumsCursorGetter() {
        Injector.inject(this);
    }

    public AlbumsCursorGetter(String artist) {
        this.artist = artist;
        Injector.inject(this);
    }

    public Cursor getAlbumsCursor(){

        if(artist != null){
            selection = MediaStore.Audio.Artists.Albums.ARTIST + " = ?";
            selectionArgs = new String[]{artist};
        }

        return contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
        );

    }

    public Album getAlbumById(long id){
        String selection = MediaStore.Audio.Albums._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);

        cursor.moveToFirst();
        return new Album(cursor.getInt(_ID),
                cursor.getString(ALBUM),
                cursor.getString(ARTIST),
                cursor.getInt(FIRST_YEAR),
                cursor.getString(ALBUM_ART),
                cursor.getInt(NUMBER_OF_SONGS));
    }

    public Cursor getAlbumsCursorByArtist(Artist artist){
        String selection = MediaStore.Audio.Albums.ARTIST + " = ?";
        String[] selectionArgs = {artist.getName()};

        return contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
    }

}
