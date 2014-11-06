package com.lwm.app.ui.fragment;

import android.content.Loader;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.player.service.CurrentSongAvailableEvent;
import com.lwm.app.helper.SongsCursorGetter;
import com.lwm.app.model.Song;
import com.lwm.app.ui.async.SongsListLoader;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnItemClick;

public class SongsListFragment extends BaseSongsListFragment {

    @Inject
    Utils utils;

    @InjectView(R.id.listView)
    ListView mListView;
    @InjectView(R.id.progress)
    ProgressBar mProgress;
    @InjectView(R.id.emptyView)
    LinearLayout mEmptyView;


    public SongsListFragment() {}

    @Override
    protected int getViewId() {
        return R.layout.fragment_list_songs;
    }

    @Override
    protected Loader<List<Song>> getSongsLoader() {
        return new SongsListLoader(getActivity(), new SongsCursorGetter(getActivity()).getSongsCursor());
    }

    @Override
    public void onResume() {
        super.onResume();
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

    @OnItemClick(R.id.listView)
    public void onItemClicked(int pos) {
        player.setQueue(songs);
        player.play(pos);
    }

    private void shuffleAll() {
        if (songs != null && !songs.isEmpty()) {
            List<Song> queue = new ArrayList<>(songs);
            player.setQueue(queue);
            player.shuffleQueue();
            player.play(0);
        } else {
            Toast.makeText(getActivity(), R.string.nothing_to_shuffle, Toast.LENGTH_LONG).show();
        }
    }

}