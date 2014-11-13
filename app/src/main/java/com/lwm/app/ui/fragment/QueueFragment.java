package com.lwm.app.ui.fragment;

import android.os.AsyncTask;
import android.view.View;

import com.lwm.app.R;
import com.lwm.app.events.player.queue.PlaylistAddedToQueueEvent;
import com.lwm.app.events.player.queue.QueueShuffledEvent;
import com.lwm.app.events.player.queue.SongAddedToQueueEvent;
import com.lwm.app.events.player.queue.SongRemovedFromQueueEvent;
import com.lwm.app.events.ui.QueueLoadingEvent;
import com.lwm.app.model.Song;
import com.lwm.app.ui.async.QueueLoaderTask;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.OnItemClick;

public class QueueFragment extends BaseSongsListFragment {

    @Override
    protected int getViewId() {
        return R.layout.fragment_list_queue;
    }

    @Override
    protected AsyncTask<Void, Void, List<Song>> getSongsLoaderTask() {
        return new QueueLoaderTask();
    }

    @OnItemClick(R.id.listView)
    public void onItemClicked(int pos) {
        player.play(pos);
    }

    @Subscribe
    public void onQueueLoadingEvent(QueueLoadingEvent event) {
        switch (event.getState()) {
            case LOADING:
                mProgress.setVisibility(View.VISIBLE);
                break;
            case LOADED:
                mProgress.setVisibility(View.GONE);
                if (!event.getList().isEmpty()) {
                    initAdapter(event.getList());
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Subscribe
    public void onSongAddedToQueueEvent(SongAddedToQueueEvent event) {
        songs.add(event.getSong());
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onPlaylistAddedToQueueEvent(PlaylistAddedToQueueEvent event) {
        songs.addAll(event.getAppendedSongs());
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onQueueShuffled(QueueShuffledEvent event) {
        songs.clear();
        songs.addAll(event.getQueue());
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onSongAddedToQueue(SongAddedToQueueEvent event) {
        songs.add(event.getSong());
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onSongRemovedFromQueue(SongRemovedFromQueueEvent event) {
        songs.remove(event.getSong());
        adapter.notifyDataSetChanged();
    }

}