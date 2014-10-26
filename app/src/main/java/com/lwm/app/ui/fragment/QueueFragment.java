package com.lwm.app.ui.fragment;

import android.support.v4.content.Loader;
import android.view.MenuItem;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.events.player.PlaybackStartedEvent;
import com.lwm.app.events.player.PlaylistAddedToQueueEvent;
import com.lwm.app.events.player.QueueShuffledEvent;
import com.lwm.app.events.player.SongAddedToQueueEvent;
import com.lwm.app.events.player.SongRemovedFromQueueEvent;
import com.lwm.app.events.player.service.CurrentSongAvailableEvent;
import com.lwm.app.events.player.service.LocalPlayerServiceAvailableEvent;
import com.lwm.app.model.Song;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.ui.async.LocalQueueLoader;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.OnItemClick;

public class QueueFragment extends BaseSongsListFragment {

    private LocalPlayerService player;

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
        return new LocalQueueLoader(getActivity(), player);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_shuffle:
                if(App.localPlayerActive()) {
                    player = App.getLocalPlayerService();
                    player.shuffleQueue();
                    player.play(0);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    public void onLocalPlayerAvailable(LocalPlayerServiceAvailableEvent event) {
        player = event.getService();
        initAdapter();
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