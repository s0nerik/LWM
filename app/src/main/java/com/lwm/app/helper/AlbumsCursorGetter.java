package com.lwm.app.helper;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

public class AlbumsCursorGetter {

    private String artist;
    private Context caller;

    private final String[] projection = {
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.Albums.ARTIST,
//            MediaStore.Audio.Albums.ALBUM_KEY
//            MediaStore.Audio.Albums.ALBUM_ID
    };

    private String selection = null;
    private String[] selectionArgs = null;

    public static final int _ID          = 0;
    public static final int ALBUM        = 1;
    public static final int ALBUM_ART    = 2;
    public static final int ARTIST       = 3;
//    public static final int ALBUM_KEY    = 4;
//    public static final int ALBUM_ID     = 4;

    public AlbumsCursorGetter(Context caller) {
        this.caller = caller;
    }

    public AlbumsCursorGetter(Context caller, String artist) {
        this.caller = caller;
        this.artist = artist;
    }

    public Cursor getAlbums(){

        if(artist != null){
            selection = MediaStore.Audio.Artists.Albums.ARTIST + " = ?";
            selectionArgs = new String[]{artist};
        }

        return caller.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
        );

    }

}
