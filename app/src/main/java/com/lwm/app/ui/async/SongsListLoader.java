package com.lwm.app.ui.async;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

import com.lwm.app.model.Playlist;
import com.lwm.app.model.Song;

import java.util.List;

public class SongsListLoader extends AsyncTaskLoader<List<Song>> {

    private Context context;
    private Cursor cursor;

    public SongsListLoader(Context context, Cursor cursor) {
        super(context);
        this.context = context;
        this.cursor = cursor;
    }

    @Override
    public List<Song> loadInBackground() {
        return Playlist.fromCursor(cursor);
    }

}
