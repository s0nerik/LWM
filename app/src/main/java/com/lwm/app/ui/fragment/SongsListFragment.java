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

public class SongsListFragment extends BaseSongsListFragment {

    @InjectView(R.id.fab)
    FloatingActionButton mFab;

    @Override
    protected int getViewId() {
        return R.layout.fragment_list_songs;
    }

//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        ItemClickSupport itemClickSupport = ItemClickSupport.addTo(mTwoWayView);
//        itemClickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
//            @Override
//            public void onItemClick(RecyclerView recyclerView, View view, int i, long l) {
//                player.setQueue(songs);
//                player.play(i);
//
//                adapter.setSelection(i);
//            }
//        });
//    }

    @Override
    protected AsyncTask<Void, Void, List<Song>> getSongsLoaderTask() {
        return new SongsLoaderTask();
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
                    mFab.attachToRecyclerView(mTwoWayView);
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

}