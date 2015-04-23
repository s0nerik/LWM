package com.lwm.app.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lwm.app.R;
import com.lwm.app.adapter.SongsListAdapter;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.player.service.CurrentSongAvailableEvent;
import com.lwm.app.events.ui.ShouldShuffleSongsEvent;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public abstract class BaseSongsListFragment extends DaggerOttoOnResumeFragment {

    @InjectView(R.id.twoWayView)
    RecyclerView mTwoWayView;
    @InjectView(R.id.progress)
    ProgressBar mProgress;
    @InjectView(R.id.emptyView)
    View mEmptyView;

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
        ButterKnife.inject(this, v);
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mTwoWayView.setLayoutManager(layoutManager);
//        mTwoWayView.setLayoutManager(new ListLayoutManager(getActivity(), TwoWayLayoutManager.Orientation.VERTICAL));
        mTwoWayView.setHasFixedSize(true);
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
//        ButterKnife.reset(this);
    }

    protected void setSelection(Song song) {
        if (songs != null) {
            int index = songs.indexOf(song);
            if (index >= 0) {
                setSelection(index);
            }
        }
    }

    private void setSelection(int position) {
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
        public void onShuffleSongs(ShouldShuffleSongsEvent event) {
            shuffleAll();
        }

    }

}