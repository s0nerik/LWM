package com.lwm.app.ui.async;

import com.lwm.app.events.ui.SongsListLoadingEvent;
import com.lwm.app.helper.db.SongsCursorGetter;
import com.lwm.app.model.Playlist;
import com.lwm.app.model.Song;

import java.util.List;

import static com.lwm.app.events.ui.SongsListLoadingEvent.State.LOADED;
import static com.lwm.app.events.ui.SongsListLoadingEvent.State.LOADING;

public class SongsLoaderTask extends BusAsyncTask<List<Song>> {

    @Override
    protected void onPreExecute() {
        bus.post(new SongsListLoadingEvent(LOADING));
    }

    @Override
    protected List<Song> doInBackground(Void... params) {
        return Playlist.fromCursor(new SongsCursorGetter().getSongsCursor());
    }

    @Override
    protected void onPostExecute(List<Song> songs) {
        bus.post(new SongsListLoadingEvent(songs, LOADED));
    }
}
