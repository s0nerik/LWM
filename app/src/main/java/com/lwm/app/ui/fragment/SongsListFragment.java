package com.lwm.app.ui.fragment;

import android.os.AsyncTask;
import android.view.View;

import com.lwm.app.R;
import com.lwm.app.events.ui.SongsListLoadingEvent;
import com.lwm.app.model.Song;
import com.lwm.app.ui.async.SongsLoaderTask;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class SongsListFragment extends BaseSongsListFragment {

    @InjectView(R.id.fab)
    FloatingActionButton mFab;

    @Override
    protected int getViewId() {
        return R.layout.fragment_list_songs;
    }

    @Override
    protected AsyncTask<Void, Void, List<Song>> getSongsLoaderTask() {
        return new SongsLoaderTask();
    }

    @OnItemClick(R.id.listView)
    public void onItemClicked(int pos) {
        player.setQueue(songs);
        player.play(pos);
    }

    @OnClick(R.id.fab)
    public void onFabClicked() {
        shuffleAll();
    }

    @Subscribe
    public void onSongsLoadingEvent(SongsListLoadingEvent event) {
        switch (event.getState()) {
            case LOADING:
                mProgress.setVisibility(View.VISIBLE);
                mFab.hide();
                break;
            case LOADED:
                mProgress.setVisibility(View.GONE);
                songs = event.getList();
                if (!event.getList().isEmpty()) {
                    initAdapter(songs);
                    setSelection(currentSong);
                    mFab.show(true);
                    mFab.attachToListView(mListView);
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

}