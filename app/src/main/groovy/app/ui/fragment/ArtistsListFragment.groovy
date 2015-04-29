package app.ui.fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import app.Injector
import app.adapter.ArtistWrappersAdapter
import app.model.ArtistWrapper
import app.ui.async.MusicLoaderService
import app.ui.base.DaggerOttoOnCreateFragment
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.InjectView
import com.joanzapata.android.asyncservice.api.annotation.InjectService
import com.joanzapata.android.asyncservice.api.annotation.OnMessage
import com.joanzapata.android.asyncservice.api.internal.AsyncService
import app.R
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
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
        SwissKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        mProgress.setVisibility(View.VISIBLE);
        musicLoaderService.loadAllArtists();
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