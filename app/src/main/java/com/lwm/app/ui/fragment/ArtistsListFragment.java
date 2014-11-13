package com.lwm.app.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.lwm.app.Injector;
import com.lwm.app.R;
import com.lwm.app.adapter.ArtistWrappersAdapter;
import com.lwm.app.events.ui.ArtistsListLoadingEvent;
import com.lwm.app.model.ArtistWrapperList;
import com.lwm.app.ui.async.ArtistsLoaderTask;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ArtistsListFragment extends DaggerOttoOnCreateFragment {

    @InjectView(R.id.empty)
    LinearLayout mEmpty;
    @InjectView(R.id.grid)
    RecyclerView mGrid;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

    public ArtistsListFragment() {
        Injector.inject(this);
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
        mGrid.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        new ArtistsLoaderTask().execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Subscribe
    public void onArtistsLoadingEvent(ArtistsListLoadingEvent event) {
        switch (event.getState()) {
            case LOADING:
                mProgress.setVisibility(View.VISIBLE);
                break;
            case LOADED:
                mProgress.setVisibility(View.GONE);
                if (!event.getList().getArtistWrappers().isEmpty()) {
                    initAdapter(event.getList());
                } else {
                    mEmpty.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void initAdapter(ArtistWrapperList list) {
        ArtistWrappersAdapter adapter = new ArtistWrappersAdapter(list);
        mGrid.setAdapter(adapter);
        mGrid.setHasFixedSize(true);
    }


}