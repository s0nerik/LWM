package com.lwm.app.ui.async;

import android.database.Cursor;

import com.lwm.app.events.ui.ArtistsListLoadingEvent;
import com.lwm.app.helper.db.ArtistsCursorGetter;
import com.lwm.app.model.ArtistWrapperList;

public class ArtistsLoaderTask extends BusAsyncTask<ArtistWrapperList> {

    @Override
    protected void onPreExecute() {
        bus.post(new ArtistsListLoadingEvent(ArtistsListLoadingEvent.State.LOADING));
    }

    @Override
    protected ArtistWrapperList doInBackground(Void... params) {
        Cursor artists = new ArtistsCursorGetter().getArtistsCursor();
        return new ArtistWrapperList(artists);
    }

    @Override
    protected void onPostExecute(ArtistWrapperList artistWrapperList) {
        bus.post(new ArtistsListLoadingEvent(artistWrapperList, ArtistsListLoadingEvent.State.LOADED));
    }
}