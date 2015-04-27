package app.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import app.adapter.SongsListAdapter
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.events.ui.ShouldShuffleSongsEvent
import app.model.Song
import app.player.LocalPlayer
import app.ui.base.DaggerOttoOnResumeFragment
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.InjectView
import com.lwm.app.R
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller

import javax.inject.Inject

@CompileStatic
public abstract class BaseSongsListFragment extends DaggerOttoOnResumeFragment {

    @InjectView(R.id.twoWayView)
    RecyclerView mTwoWayView;
    @InjectView(R.id.progress)
    ProgressBar mProgress;
    @InjectView(R.id.emptyView)
    View mEmptyView;
    @InjectView(R.id.fast_scroller)
    VerticalRecyclerViewFastScroller fastScroller;

    @Inject
    protected LocalPlayer player;

    protected List<Song> songs;
    protected Song currentSong;

    protected SongsListAdapter adapter;

    private LinearLayoutManager layoutManager;

    protected abstract int getViewId();
    protected abstract void loadSongs();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setBusListeners(new BusListener(), this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = View.inflate(getActivity(), getViewId(), null);
        SwissKnife.inject(this, v);
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mTwoWayView.setLayoutManager(layoutManager);
        mTwoWayView.setHasFixedSize(true);
        fastScroller.setRecyclerView(mTwoWayView);
        mTwoWayView.addOnScrollListener(fastScroller.getOnScrollListener());
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadSongs();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

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
        int prevSelection = adapter.getSelection();
        adapter.setSelection(position);
        if (position < layoutManager.findFirstCompletelyVisibleItemPosition() ||
                position > layoutManager.findLastCompletelyVisibleItemPosition()) {
            if (Math.abs(prevSelection - adapter.getSelection()) <= 100) {
                mTwoWayView.smoothScrollToPosition(position);
            } else {
                mTwoWayView.scrollToPosition(position);
            }
        }
    }

    protected void initAdapter(List<Song> songs) {
        adapter = new SongsListAdapter(getActivity(), songs);
        mTwoWayView.setAdapter(adapter);
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