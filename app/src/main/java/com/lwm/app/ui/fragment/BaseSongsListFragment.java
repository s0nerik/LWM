package com.lwm.app.ui.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
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

public abstract class BaseSongsListFragment extends DaggerOttoOnCreateFragment {

    @InjectView(R.id.listView)
    ListView mListView;
    @InjectView(R.id.progress)
    ProgressBar mProgress;
    @InjectView(R.id.emptyView)
    LinearLayout mEmptyView;

    @Inject
    protected LocalPlayer player;

    protected List<Song> songs;
    protected Song currentSong;

    protected SongsListAdapter adapter;

    protected abstract int getViewId();
    protected abstract AsyncTask<Void, Void, List<Song>> getSongsLoaderTask();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setBusListeners(new BusListener(), this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(getViewId(), container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getSongsLoaderTask().execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    protected void setSelection(Song song) {
        if (songs != null) {
            int index = songs.indexOf(song);
            setSelection(index);
        }
    }

    private void setSelection(int position) {
        mListView.setItemChecked(position, true);
        mListView.setSelection(position);
    }

    protected void initAdapter(List<Song> songs) {
        adapter = new SongsListAdapter(getActivity(), player, songs);
        mListView.setAdapter(adapter);
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