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
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.InjectView
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@InjectLayout(R.layout.fragment_list_artists)
public class ArtistsListFragment extends DaggerOttoOnCreateFragment {

    @InjectView(R.id.empty)
    LinearLayout mEmpty;
    @InjectView(R.id.twoWayView)
    RecyclerView mRecyclerView;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

    @Inject
    @PackageScope
    ArtistsManager artistsManager

    private List<ArtistWrapper> artists = new ArrayList<>()

    private ArtistWrappersAdapter adapter

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false)

        adapter = new ArtistWrappersAdapter(activity, artists)
        mRecyclerView.adapter = this.adapter
        mRecyclerView.hasFixedSize = true

        loadArtists()
    }

    private loadArtists() {
        mRecyclerView.hide()
        mProgress.show()
        artistsManager.loadAllArtists().subscribe this.&onArtistsLoaded
    }

    private void onArtistsLoaded(List<ArtistWrapper> artists) {
        mProgress.hide()

        this.artists.clear()
        this.artists.addAll artists
        if (artists) {
            adapter.notifyDataSetChanged()
            mRecyclerView.show()
        } else {
            mEmpty.show()
        }
    }

}