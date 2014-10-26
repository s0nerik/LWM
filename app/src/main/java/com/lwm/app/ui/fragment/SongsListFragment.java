package com.lwm.app.ui.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.SongsListAdapter;
import com.lwm.app.events.player.PlaybackStartedEvent;
import com.lwm.app.events.player.service.CurrentSongAvailableEvent;
import com.lwm.app.events.player.service.LocalPlayerServiceAvailableEvent;
import com.lwm.app.helper.SongsCursorGetter;
import com.lwm.app.model.Song;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.ui.async.SongsListLoader;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

public class SongsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Song>> {

    @InjectView(R.id.listView)
    ListView mListView;
    @InjectView(R.id.progress)
    ProgressBar mProgress;
    @InjectView(R.id.emptyView)
    LinearLayout mEmptyView;

    private List<Song> songs;
    private LocalPlayerService player;
    private Song currentSong;

    private Loader<List<Song>> songsLoader;

    private SongsListAdapter adapter;

    public SongsListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_songs, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        App.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        App.getBus().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.songs_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Subscribe
    public void onLocalPlayerServiceAvailable(LocalPlayerServiceAvailableEvent event) {
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

    private void setSelection(Song song) {
        if (songs != null) {
            int index = songs.indexOf(song);
            setSelection(index);
        }
    }

    private void setSelection(int position) {
        mListView.setItemChecked(position, true);

        Rect scrollBounds = new Rect();
        mListView.getHitRect(scrollBounds);
        if (position < 0 || !getViewByPosition(songs.indexOf(currentSong)).getLocalVisibleRect(scrollBounds)) {
            mListView.setSelection(position);
            mListView.smoothScrollToPosition(position);
        }

    }

    @OnItemClick(R.id.listView)
    public void onItemClicked(int pos) {
        player.setQueue(songs);
        player.play(pos);
    }

    private void initAdapter() {
        Log.d(App.TAG, "initAdapter()");
        songs = new ArrayList<>();
        adapter = new SongsListAdapter(getActivity(), songs);
        mListView.setAdapter(adapter);
        songsLoader = new SongsListLoader(getActivity(), new SongsCursorGetter(getActivity()).getSongsCursor());
        getLoaderManager().initLoader(0, null, this);
        songsLoader.forceLoad();
    }

    public View getViewByPosition(int pos) {
        final int firstListItemPosition = mListView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + mListView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return mListView.getAdapter().getView(pos, null, mListView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return mListView.getChildAt(childIndex);
        }
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        Log.d(App.TAG, "onCreateLoader");
        mEmptyView.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        return songsLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        Log.d(App.TAG, "onLoadFinished()");
        mProgress.setVisibility(View.GONE);
        if (!data.isEmpty()) {
            songs.addAll(data);
            adapter.notifyDataSetChanged();
            setSelection(currentSong);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_shuffle) {
            shuffleAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
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