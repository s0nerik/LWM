package app.ui.fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import app.R
import app.adapter.ArtistsAdapter
import app.data_managers.ArtistsManager
import app.model.Artist
import app.ui.base.DaggerOttoOnCreateFragment
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.InjectView
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
@InjectLayout(R.layout.fragment_list_artists)
class ArtistsListFragment extends DaggerOttoOnCreateFragment {

    @InjectView(R.id.empty)
    LinearLayout mEmpty
    @InjectView(R.id.twoWayView)
    RecyclerView mRecyclerView
    @InjectView(R.id.progress)
    ProgressBar mProgress

    @Inject
    @PackageScope
    ArtistsManager artistsManager

    private List<Artist> artists = new ArrayList<>()

    private ArtistsAdapter adapter

    @Override
    void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView.layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        adapter = new ArtistsAdapter(activity, artists)
        mRecyclerView.adapter = adapter
        mRecyclerView.hasFixedSize = true

        loadArtists()
    }

    private loadArtists() {
        mRecyclerView.hide()
        mProgress.show()
        artistsManager.loadAllArtists().toList().subscribe this.&onArtistsLoaded
    }

    private void onArtistsLoaded(List<Artist> artists) {
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