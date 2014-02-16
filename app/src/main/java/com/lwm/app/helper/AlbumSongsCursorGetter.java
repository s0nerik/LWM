/*
package com.lwm.app.helper;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.lwm.app.model.Album;

public class AlbumSongsCursorGetter {

    private Album album;
    private Context caller;

    public AlbumSongsCursorGetter(Context caller, Album album) {
        this.caller = caller;
        this.album = album;
    }

    public Cursor getAlbumSongs(){

        String selection = MediaStore.Audio.Media.ALBUM + " = ?";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION
        };

        return caller.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                new String[]{album.getName()},
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
        );

    }

}
*/
