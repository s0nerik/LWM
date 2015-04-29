package app.ui.fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import app.R
import app.adapter.SongsListAdapter
import app.ast.InjectView
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.events.ui.ShouldShuffleSongsEvent
import app.model.Song
import app.player.LocalPlayer
import app.ui.base.DaggerOttoOnResumeFragment
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller

import javax.inject.Inject

@CompileStatic
abstract class BaseSongsListFragment extends DaggerOttoOnResumeFragment {

    @InjectView(R.id.twoWayView)
    RecyclerView recycler
    @InjectView(R.id.fast_scroller)
    VerticalRecyclerViewFastScroller fastScroller
    @InjectView(R.id.emptyView)
    View emptyView

    @Inject
    protected LocalPlayer player;

    protected List<Song> songs;
    protected Song currentSong;

    protected SongsListAdapter adapter;

    private LinearLayoutManager layoutManager;

    protected abstract void loadSongs();

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setBusListeners(new BusListener(), this)
    }

    @Override
    void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = layoutManager
        recycler.hasFixedSize = true
        fastScroller.recyclerView = recycler
        recycler.addOnScrollListener(fastScroller.getOnScrollListener())
        loadSongs()
    }

    protected void setSelection(Song song) {
        if (songs != null) {
            int index = songs.indexOf(song);
            if (index >= 0) {
                setSelection(index);
            }
        }
    }

    protected void setSelection(int position) {
        int prevSelection = adapter.selection;
        adapter.setSelection(position);
        if (position < layoutManager.findFirstCompletelyVisibleItemPosition() ||
                position > layoutManager.findLastCompletelyVisibleItemPosition()) {
            if (Math.abs(prevSelection - adapter.selection) <= 100) {
                recycler.smoothScrollToPosition(position);
            } else {
                recycler.scrollToPosition(position);
            }
        }
    }

    protected void initAdapter(List<Song> songs) {
        adapter = new SongsListAdapter(getActivity(), songs);
        recycler.setAdapter(adapter);
    }

    protected void shuffleAll() {
        if (songs != null && !songs.isEmpty()) {
            List<Song> queue = new ArrayList<>(songs);
            player.setQueue(queue);
            player.shuffleQueue();
            player.play(0);
        } else {
            Toast.makeText(getActivity(), R.string.nothing_to_shuffle, Toast.LENGTH_LONG).show();
        }
    }

    private class BusListener {
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
        public void onSongPlaybackPaused(PlaybackPausedEvent event) {
            adapter.updateEqualizerState();
        }

        @Subscribe
        public void onShuffleSongs(ShouldShuffleSongsEvent event) {
            shuffleAll();
        }

    }

}