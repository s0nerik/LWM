package app.ui.fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import app.R
import app.adapter.ArtistWrappersAdapter
import app.data_managers.ArtistsManager
import app.model.ArtistWrapper
import app.ui.base.DaggerOttoOnCreateFragment
import com.github.s0nerik.betterknife.annotations.InjectView
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class ArtistsListFragment extends DaggerOttoOnCreateFragment {

    @InjectView(R.id.empty)
    LinearLayout mEmpty;
    @InjectView(R.id.twoWayView)
    RecyclerView mRecyclerView;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

    @Inject
    ArtistsManager artistsManager

//    @InjectService
//    SongsManager musicLoaderService;

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
//        AsyncService.inject(this)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_list_artists, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        mProgress.setVisibility(View.VISIBLE);
//        musicLoaderService.loadAllArtists();

        artistsManager.loadAllArtists().subscribe this.&onArtistsLoaded
//        AndroidDSL.async this, {
//            onArtistsLoaded(new ArtistWrapperList(new ArtistsCursorGetter().getArtistsCursor()).getArtistWrappers())
//        }

    }

//    @OnMessage
//    public void onArtistsLoaded(SongsManager.ArtistsLoadedEvent event) {
//        mProgress.setVisibility(View.GONE);
//        List<ArtistWrapper> artists = event.getArtists();
//        if (!artists.isEmpty()) {
//            initAdapter(artists);
//        } else {
//            mEmpty.setVisibility(View.VISIBLE);
//        }
//    }

//    @OnUIThread
    private void onArtistsLoaded(List<ArtistWrapper> artists) {
        mProgress.visibility = View.GONE
        if (!artists.isEmpty()) {
            initAdapter(artists)
        } else {
            mEmpty.visibility = View.VISIBLE
        }
    }

    private void initAdapter(List<ArtistWrapper> list) {
        ArtistWrappersAdapter adapter = new ArtistWrappersAdapter(getActivity(), list);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setHasFixedSize(true);
    }

}