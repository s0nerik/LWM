package com.lwm.app.ui.fragment;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lwm.app.Injector;
import com.lwm.app.R;
import com.lwm.app.adapter.ArtistWrappersAdapter;
import com.lwm.app.helper.db.ArtistsCursorGetter;
import com.lwm.app.model.ArtistWrapperList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ArtistsListFragment extends Fragment {

    @InjectView(android.R.id.empty)
    LinearLayout mEmpty;
    @InjectView(R.id.grid)
    RecyclerView mGrid;

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

        artistsCursorGetter = new ArtistsCursorGetter(getActivity());
        Cursor artists = artistsCursorGetter.getArtistsCursor();
        artistsList = new ArtistWrapperList(artists);

        ArtistWrappersAdapter adapter = new ArtistWrappersAdapter(artistsList);
        mGrid.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mGrid.setAdapter(adapter);
        mGrid.setHasFixedSize(true);

//        ItemClickSupport itemClick = ItemClickSupport.addTo(mGrid);
//        itemClick.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
//            @Override
//            public void onItemClick(RecyclerView recyclerView, View view, int i, long l) {
//                Intent intent = new Intent(getActivity(), ArtistInfoActivity.class);
//                intent.putExtra("artist_id", artistsList.getArtistWrappers().get(i).getArtist().getId());
//                startActivity(intent);
//                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left_long_alpha);
//            }
//        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}