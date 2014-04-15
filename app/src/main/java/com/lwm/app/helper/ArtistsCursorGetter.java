package com.lwm.app.helper;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.lwm.app.model.Artist;

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

    public Cursor getArtistsCursor(){

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

    public Artist getArtistById(long id){
        String selection = MediaStore.Audio.Artists._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = caller.getContentResolver().query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);

        Artist artist = null;
        if(cursor.moveToFirst()){
            artist = new Artist(cursor.getInt(_ID),
                    cursor.getString(ARTIST),
                    cursor.getInt(NUMBER_OF_ALBUMS),
                    cursor.getInt(NUMBER_OF_TRACKS));
        }

        return artist;
    }

}