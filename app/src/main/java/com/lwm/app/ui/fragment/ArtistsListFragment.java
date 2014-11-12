package com.lwm.app.ui.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
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
import com.lwm.app.events.ui.ArtistsListLoadedEvent;
import com.lwm.app.helper.db.ArtistsCursorGetter;
import com.lwm.app.model.ArtistWrapperList;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ArtistsListFragment extends DaggerOttoFragment {

    @InjectView(android.R.id.empty)
    LinearLayout mEmpty;
    @InjectView(R.id.grid)
    RecyclerView mGrid;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

    private ArtistWrapperList artistsList;
    private ArtistsCursorGetter artistsCursorGetter;

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

        new LoadAlbumsTask().execute(getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Subscribe
    public void onArtistsLoaded(ArtistsListLoadedEvent event) {
        mProgress.setVisibility(View.GONE);
        if (!event.getList().getArtistWrappers().isEmpty()) {
            initAdapter(event.getList());
        } else {
            mEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void initAdapter(ArtistWrapperList list) {
        ArtistWrappersAdapter adapter = new ArtistWrappersAdapter(list);
        mGrid.setAdapter(adapter);
        mGrid.setHasFixedSize(true);
    }

    private class LoadAlbumsTask extends AsyncTask<Context, Void, ArtistWrapperList> {

        @Override
        protected void onPreExecute() {
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArtistWrapperList doInBackground(Context... params) {
            artistsCursorGetter = new ArtistsCursorGetter(params[0]);
            Cursor artists = artistsCursorGetter.getArtistsCursor();
            return new ArtistWrapperList(artists);
        }

        @Override
        protected void onPostExecute(ArtistWrapperList artistWrapperList) {
            bus.post(new ArtistsListLoadedEvent(artistWrapperList));
        }
    }
}