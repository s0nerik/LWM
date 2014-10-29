package com.lwm.app.ui.fragment;

import android.support.v4.content.Loader;
import android.view.MenuItem;

import com.lwm.app.R;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.player.queue.PlaylistAddedToQueueEvent;
import com.lwm.app.events.player.queue.QueueShuffledEvent;
import com.lwm.app.events.player.queue.SongAddedToQueueEvent;
import com.lwm.app.events.player.queue.SongRemovedFromQueueEvent;
import com.lwm.app.events.player.service.CurrentSongAvailableEvent;
import com.lwm.app.model.Song;
import com.lwm.app.ui.async.LocalQueueLoader;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.OnItemClick;

public class QueueFragment extends BaseSongsListFragment {

    public QueueFragment() {}

    @Override
    protected int getViewId() {
        return R.layout.fragment_list_queue;
    }

    @Override
    protected int getMenuId() {
        return R.menu.queue;
    }

    @Override
    protected Loader<List<Song>> getSongsLoader() {
        return new LocalQueueLoader(getActivity());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_shuffle:
                player.shuffleQueue();
                player.play(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initAdapter();
    }

    @OnItemClick(R.id.listView)
    public void onItemClicked(int pos) {
        player.play(pos);
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
    public void onCurrentSongAvailable(CurrentSongAvailableEvent event) {
        currentSong = event.getSong();
        setSelection(currentSong);
    }

    @Subscribe
    public void onSongPlaybackStarted(PlaybackStartedEvent event) {
        currentSong = event.getSong();
        setSelection(currentSong);
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