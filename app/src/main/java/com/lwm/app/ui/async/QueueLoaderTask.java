package com.lwm.app.ui.async;

import com.lwm.app.Injector;
import com.lwm.app.events.ui.QueueLoadingEvent;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;

import java.util.List;

import javax.inject.Inject;


public class QueueLoaderTask extends BusAsyncTask<List<Song>> {

    @Inject
    LocalPlayer player;

    public QueueLoaderTask() {
        Injector.inject(this);
    }

    @Override
    protected void onPreExecute() {
        bus.post(new QueueLoadingEvent(QueueLoadingEvent.State.LOADING));
    }

    @Override
    protected List<Song> doInBackground(Void... params) {
        return player.getQueue();
    }

    @Override
    protected void onPostExecute(List<Song> songs) {
        bus.post(new QueueLoadingEvent(songs, QueueLoadingEvent.State.LOADED));
    }
}
