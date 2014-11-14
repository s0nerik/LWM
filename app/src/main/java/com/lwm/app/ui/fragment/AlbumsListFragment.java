package com.lwm.app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.lwm.app.R;
import com.lwm.app.adapter.AlbumsAdapter;
import com.lwm.app.events.ui.AlbumsListLoadingEvent;
import com.lwm.app.model.Album;
import com.lwm.app.model.AlbumsList;
import com.lwm.app.model.Artist;
import com.lwm.app.ui.activity.AlbumInfoActivity;
import com.lwm.app.ui.async.AlbumsLoaderTask;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

public class AlbumsListFragment extends DaggerOttoOnCreateFragment {

    @InjectView(R.id.grid)
    GridView mGrid;
    @InjectView(android.R.id.empty)
    LinearLayout mEmpty;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

    private AlbumsList albumsList;
    private Artist artist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().getString("artist") != null) {
            artist = new Gson().fromJson(getArguments().getString("artist"), Artist.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_albums, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new AlbumsLoaderTask(artist).execute();
    }

    @Subscribe
    public void onAlbumsLoadingEvent(AlbumsListLoadingEvent event) {
        switch (event.getState()) {
            case LOADING:
                mProgress.setVisibility(View.VISIBLE);
                break;
            case LOADED:
                mProgress.setVisibility(View.GONE);
                initAdapter(event.getList());
                break;
        }
    }

    @OnItemClick(R.id.grid)
    public void onItemClick(int position) {
        Intent intent = new Intent(getActivity(), AlbumInfoActivity.class);
        intent.putExtra("album_id", albumsList.getAlbums().get(position).getId());
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left_long_alpha);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void initAdapter(List<Album> albums) {
        ListAdapter adapter = new AlbumsAdapter(getActivity(), albums);

        if (adapter.getCount() > 0) {
            mGrid.setVisibility(View.VISIBLE);
            mGrid.setAdapter(adapter);
        } else {
            mGrid.setVisibility(View.GONE);
            mEmpty.setVisibility(View.VISIBLE);
        }
    }
}
