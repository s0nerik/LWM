package app.ui.fragment

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import app.R
import app.adapter.albums.ArtistAlbumItem
import app.adapter.artists.ArtistItem
import app.adapter.artists.ArtistsAdapter
import app.events.ui.FilterLocalMusicCommand
import app.helper.CollectionManager
import app.model.Artist
import app.ui.base.DaggerOttoOnCreateFragment
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.InjectView
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
@InjectLayout(R.layout.fragment_list_artists)
class ArtistsListFragment extends DaggerOttoOnCreateFragment implements SortableFragment {

    @InjectView(R.id.empty)
    LinearLayout mEmpty
    @InjectView(R.id.twoWayView)
    RecyclerView mRecyclerView
    @InjectView(R.id.progress)
    ProgressBar mProgress

    @Inject
    @PackageScope
    CollectionManager collectionManager

    private List<ArtistItem> artists = new ArrayList<>()
    private List<ArtistItem> filteredArtists = new ArrayList<>()

    private ArtistsAdapter adapter

    private int sortActionId
    private boolean orderAscending

    @Override
    void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView.layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        adapter = new ArtistsAdapter(artists)
        mRecyclerView.adapter = adapter
        mRecyclerView.hasFixedSize = true

        loadArtists()
    }

    private loadArtists() {
        mRecyclerView.hide()
        mProgress.show()
        onArtistsLoaded collectionManager.artists
    }

    private void onArtistsLoaded(List<Artist> artists) {
        mProgress.hide()

        this.artists.clear()
        this.artists.addAll artists.collect {
            def item = new ArtistItem(it)
            item.setSubItems collectionManager.getAlbums(it).collect { new ArtistAlbumItem(it) }
            item
        }

        filteredArtists = new ArrayList(this.artists)

        if (artists) {
            adapter.notifyDataSetChanged()
            mRecyclerView.show()
        } else {
            mEmpty.show()
        }
    }

    @Subscribe
    void onEvent(FilterLocalMusicCommand cmd) {
        adapter.searchText = cmd.constraint
        adapter.filterItems(filteredArtists)
    }

    @Override
    int getSortMenuId() {
        return 0
    }

    @Override
    int getSortActionId() {
        return sortActionId
    }

    @Override
    void setSortActionId(@IdRes int id) {
        sortActionId = id
    }

    @Override
    boolean isOrderAscending() {
        return orderAscending
    }

    @Override
    void setOrderAscending(boolean value) {
        orderAscending = value
    }

    @Override
    int getSortIconId() {
        return 0
    }

    @Override
    void sortItems() {

    }
}