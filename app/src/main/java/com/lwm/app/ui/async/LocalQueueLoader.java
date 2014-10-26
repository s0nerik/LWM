package com.lwm.app.ui.async;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.lwm.app.model.Song;
import com.lwm.app.service.LocalPlayerService;

import java.util.List;

public class LocalQueueLoader extends AsyncTaskLoader<List<Song>> {

    private LocalPlayerService player;

    public LocalQueueLoader(Context context, LocalPlayerService player) {
        super(context);
        this.player = player;
    }

    @Override
    public List<Song> loadInBackground() {
        return player.getQueue();
    }

}
