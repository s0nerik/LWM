package com.lwm.app.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.joanzapata.android.asyncservice.api.annotation.InjectService;
import com.joanzapata.android.asyncservice.api.annotation.OnMessage;
import com.joanzapata.android.asyncservice.api.internal.AsyncService;
import com.lwm.app.Injector;
import com.lwm.app.R;
import com.lwm.app.adapter.ArtistWrappersAdapter;
import com.lwm.app.model.ArtistWrapper;
import com.lwm.app.ui.async.MusicLoaderService;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ArtistsListFragment extends DaggerOttoOnCreateFragment {

    @InjectView(R.id.empty)
    LinearLayout mEmpty;
    @InjectView(R.id.twoWayView)
    RecyclerView mRecyclerView;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

    @InjectService
    MusicLoaderService musicLoaderService;

    public ArtistsListFragment() {
        Injector.inject(this);
        AsyncService.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_list_artists, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        mProgress.setVisibility(View.VISIBLE);
        musicLoaderService.loadAllArtists();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        ButterKnife.reset(this);
    }

    @OnMessage
    public void onArtistsLoaded(MusicLoaderService.ArtistsLoadedEvent event) {
        mProgress.setVisibility(View.GONE);
        List<ArtistWrapper> artists = event.getArtists();
        if (!artists.isEmpty()) {
            initAdapter(artists);
        } else {
            mEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void initAdapter(List<ArtistWrapper> list) {
        ArtistWrappersAdapter adapter = new ArtistWrappersAdapter(getActivity(), list);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setHasFixedSize(true);
    }

}