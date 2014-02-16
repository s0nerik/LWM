package com.lwm.app.helper;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

public class ArtistsCursorGetter {

    private Context caller;

    private final String[] projection = {
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
    };

    public static final int _ID              = 0;
    public static final int ARTIST           = 1;
    public static final int NUMBER_OF_ALBUMS = 2;
    public static final int NUMBER_OF_TRACKS = 3;

    public ArtistsCursorGetter(Context caller) {
        this.caller = caller;
    }

    public Cursor getArtists(){

//        String[] projection = {
//                MediaStore.Audio.Artists._ID,
//                MediaStore.Audio.Artists.ARTIST,
//                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
//                MediaStore.Audio.Artists.NUMBER_OF_TRACKS
//        };

        return caller.getContentResolver().query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
        );
    }

}