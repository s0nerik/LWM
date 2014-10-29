package com.lwm.app.ui.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.SongsListAdapter;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.ui.base.DaggerFragment;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public abstract class BaseSongsListFragment extends DaggerFragment implements LoaderManager.LoaderCallbacks<List<Song>> {

    @InjectView(R.id.listView)
    ListView mListView;
    @Optional @InjectView(R.id.progress)
    ProgressBar mProgress;
    @InjectView(R.id.emptyView)
    LinearLayout mEmptyView;

    @Inject
    Bus bus;

    @Inject
    protected LocalPlayer player;

    protected List<Song> songs;
    protected Song currentSong;

    protected SongsListAdapter adapter;

    private Loader<List<Song>> songsLoader;

    public BaseSongsListFragment() {}

    protected abstract int getViewId();
    protected abstract int getMenuId();
    protected abstract Loader<List<Song>> getSongsLoader();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(getViewId(), container, false);
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
        bus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(getMenuId(), menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    protected void setSelection(Song song) {
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

    protected void initAdapter() {
        Log.d(App.TAG, "initAdapter()");
        songs = new ArrayList<>();
        adapter = new SongsListAdapter(getActivity(), player, songs);
        mListView.setAdapter(adapter);
        songsLoader = getSongsLoader();
        getLoaderManager().initLoader(0, null, this);
        songsLoader.forceLoad();
    }

    protected View getViewByPosition(int pos) {
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
        if (mProgress != null) mProgress.setVisibility(View.VISIBLE);
        return songsLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        Log.d(App.TAG, "onLoadFinished()");
        if (mProgress != null) mProgress.setVisibility(View.GONE);
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

}