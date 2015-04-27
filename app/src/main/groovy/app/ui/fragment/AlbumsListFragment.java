package app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;

import com.arasthel.swissknife.SwissKnife;
import com.arasthel.swissknife.annotations.InjectView;
import com.arasthel.swissknife.annotations.OnItemClick;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.joanzapata.android.asyncservice.api.annotation.InjectService;
import com.joanzapata.android.asyncservice.api.annotation.OnMessage;
import com.joanzapata.android.asyncservice.api.internal.AsyncService;
import com.lwm.app.R;
import com.tale.prettybundle.Activities;

import java.util.ArrayList;
import java.util.List;

import app.adapter.AlbumsAdapter;
import app.model.Album;
import app.model.Artist;
import app.ui.async.MusicLoaderService;
import app.ui.base.DaggerOttoOnResumeFragment;

public class AlbumsListFragment extends DaggerOttoOnResumeFragment {

    @InjectView(R.id.grid)
    GridView mGrid;
    @InjectView(android.R.id.empty)
    LinearLayout mEmpty;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

    @InjectService
    MusicLoaderService musicLoaderService;

    @Arg(required = false)
    Artist artist;

//    @Extra
//    Artist artist;

    private List<Album> albums = new ArrayList<>();

    public AlbumsListFragment() {
        AsyncService.inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        PrettyBundle.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_albums, container, false);
        SwissKnife.inject(this, v);
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
        Intent intent = Activities.createAlbumInfoActivityIntent(getActivity(), albums.get(position));
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left_long_alpha);
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
