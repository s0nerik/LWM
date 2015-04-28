package app.ui.fragment;

import android.view.View;

import app.R;
import app.events.player.queue.PlaylistAddedToQueueEvent;
import app.events.player.queue.QueueShuffledEvent;
import app.events.player.queue.SongAddedToQueueEvent;
import app.events.player.queue.SongRemovedFromQueueEvent;
import app.player.LocalPlayer;
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic;

import javax.inject.Inject;

@CompileStatic
public class QueueFragment extends BaseSongsListFragment {

    @Inject
    LocalPlayer player;

    @Override
    protected int getViewId() {
        return R.layout.fragment_list_queue;
    }

    @Override
    protected void loadSongs() {
        songs = player.getQueue();
        if (!songs.isEmpty()) {
            initAdapter(songs);
            setSelection(currentSong);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe
    public void onPlaylistAddedToQueueEvent(PlaylistAddedToQueueEvent event) {
        int startIndex = songs.size();
        songs.addAll(event.getAppendedSongs());

        adapter.notifyItemRangeInserted(startIndex, event.getAppendedSongs().size());
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
        adapter.notifyItemInserted(songs.size() - 1);
    }

    @Subscribe
    public void onSongRemovedFromQueue(SongRemovedFromQueueEvent event) {
        songs.remove(event.getSong());
        adapter.notifyItemRemoved(songs.size());
    }

}