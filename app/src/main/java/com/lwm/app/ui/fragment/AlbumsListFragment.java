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
import com.joanzapata.android.asyncservice.api.annotation.InjectService;
import com.joanzapata.android.asyncservice.api.annotation.OnMessage;
import com.joanzapata.android.asyncservice.api.internal.AsyncService;
import com.lwm.app.R;
import com.lwm.app.adapter.AlbumsAdapter;
import com.lwm.app.model.Album;
import com.lwm.app.model.Artist;
import com.lwm.app.ui.activity.AlbumInfoActivity;
import com.lwm.app.ui.async.MusicLoaderService;
import com.lwm.app.ui.base.DaggerOttoOnResumeFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

public class AlbumsListFragment extends DaggerOttoOnResumeFragment {

    @InjectView(R.id.grid)
    GridView mGrid;
    @InjectView(android.R.id.empty)
    LinearLayout mEmpty;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

    @InjectService
    MusicLoaderService musicLoaderService;

    private List<Album> albums = new ArrayList<>();
    private Artist artist;

    public AlbumsListFragment() {
        AsyncService.inject(this);
    }

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
        mProgress.setVisibility(View.VISIBLE);
        musicLoaderService.loadAllAlbums(artist);
    }

    @OnMessage
    public void onAlbumsLoaded(MusicLoaderService.AlbumsLoadedEvent event) {
        mProgress.setVisibility(View.GONE);
        albums = event.getAlbums();
        initAdapter(albums);
    }

    @OnItemClick(R.id.grid)
    public void onItemClick(int position) {
        Intent intent = new Intent(getActivity(), AlbumInfoActivity.class);
        intent.putExtra("album_id", albums.get(position).getId());
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
