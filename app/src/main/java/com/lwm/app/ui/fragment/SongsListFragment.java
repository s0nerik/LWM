package com.lwm.app.ui.fragment;

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
import com.lwm.app.Utils;
import com.lwm.app.adapter.SongsListAdapter;
import com.lwm.app.events.player.binding.LocalPlayerServiceBoundEvent;
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
    private int currentPosition = -1;

    private Loader<List<Song>> songsLoader;

    private final static int SMOOTH_SCROLL_MAX = 50;
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(App.TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);

        if (App.isLocalPlayerServiceBound()) {
            player = App.getLocalPlayerService();
            initAdapter();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        App.getBus().register(this);
        if (App.localPlayerActive()) {
            player = App.getLocalPlayerService();
            highlightCurrentSong();
        }
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
    public void onServiceBound(LocalPlayerServiceBoundEvent event) {
        player = event.getLocalPlayerService();
        initAdapter();
    }

    public void highlightCurrentSong() {
        int pos = Utils.getCurrentSongPosition(songs);
        setSelection(pos);
    }

    private void setSelection(int position) {
        mListView.setItemChecked(position, true);

        Log.d(App.TAG, "setSelection: " + currentPosition);

        if (Math.abs(position - currentPosition) <= SMOOTH_SCROLL_MAX) {
            mListView.smoothScrollToPosition(position);
            mListView.setSelection(position);
        } else {
            mListView.setSelection(position);
        }
        currentPosition = position;
    }

    @OnItemClick(R.id.listView)
    public void onItemClicked(int pos) {
        if (App.isLocalPlayerServiceBound()) {
            player.setQueue(songs);
            player.play(pos);
        }
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
            highlightCurrentSong();
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
            highlightCurrentSong();
        } else {
            Toast.makeText(getActivity(), R.string.nothing_to_shuffle, Toast.LENGTH_LONG).show();
        }
    }

}