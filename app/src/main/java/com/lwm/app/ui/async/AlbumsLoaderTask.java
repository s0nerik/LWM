package com.lwm.app.ui.async;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.lwm.app.events.ui.AlbumsListLoadingEvent;
import com.lwm.app.helper.db.AlbumsCursorGetter;
import com.lwm.app.model.AlbumsList;
import com.lwm.app.model.Artist;

public class AlbumsLoaderTask extends BusAsyncTask<AlbumsList> {

    private Artist artist;

    public AlbumsLoaderTask(@Nullable Artist artist) {
        super();
        this.artist = artist;
    }

    @Override
    protected void onPreExecute() {
        bus.post(new AlbumsListLoadingEvent(AlbumsListLoadingEvent.State.LOADING));
    }

    @Override
    protected AlbumsList doInBackground(Void... params) {
        AlbumsCursorGetter cursorGetter = new AlbumsCursorGetter();
        Cursor albums;

        if (artist == null) {
            albums = cursorGetter.getAlbumsCursor();
        } else {
            albums = cursorGetter.getAlbumsCursorByArtist(artist);
        }

        return new AlbumsList(albums);
    }

    @Override
    protected void onPostExecute(AlbumsList list) {
        bus.post(new AlbumsListLoadingEvent(list.getAlbums(), AlbumsListLoadingEvent.State.LOADED));
    }
}