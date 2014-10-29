package com.lwm.app.ui.async;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.lwm.app.Injector;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;

import java.util.List;

import javax.inject.Inject;

public class LocalQueueLoader extends AsyncTaskLoader<List<Song>> {

    @Inject
    LocalPlayer player;

    public LocalQueueLoader(Context context) {
        super(context);
        Injector.inject(this);
    }

    @Override
    public List<Song> loadInBackground() {
        return player.getQueue();
    }

}
