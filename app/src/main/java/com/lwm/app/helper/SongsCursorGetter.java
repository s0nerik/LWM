package com.lwm.app.helper;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.lwm.app.lib.QuickAdapter;

public class SongsCursorGetter implements QuickAdapter.DataSource{

    private Context caller;

    private String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
    private String[] projection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID
    };

    public static final int _ID          = 0;
    public static final int TITLE        = 1;
    public static final int ARTIST       = 2;
    public static final int ALBUM        = 3;
    public static final int DURATION     = 4;
    public static final int DATA         = 5;
    public static final int DISPLAY_NAME = 6;
    public static final int SIZE         = 7;
    public static final int ALBUM_ID     = 8;
    public static final int ARTIST_ID    = 9;

    public SongsCursorGetter(Context caller){
        this.caller = caller;
    }

    public Cursor getSongs(){

        return caller.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.ARTIST + " ASC, "
                        + MediaStore.Audio.Media.ALBUM_ID + " ASC, "
                        + MediaStore.Audio.Media.TRACK + " ASC, "
                        + MediaStore.Audio.Media.DISPLAY_NAME + " ASC"
        );

    }

    public Cursor getSongsRandomOrder(){

        return caller.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                "random()"
        );

    }

    @Override
    public Cursor getRowIds() {
        return caller.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID},
                selection,
                null,
                MediaStore.Audio.Media.ARTIST + " ASC, "
                        + MediaStore.Audio.Media.ALBUM_ID + " ASC, "
                        + MediaStore.Audio.Media.TRACK + " ASC, "
                        + MediaStore.Audio.Media.DISPLAY_NAME + " ASC"
        );
    }

    @Override
    public Cursor getRowById(long rowId) {
        return caller.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Audio.Media._ID+" = "+rowId,
                null,
                null
        );
    }
}
