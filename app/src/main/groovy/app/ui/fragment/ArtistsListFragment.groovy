package app.ui.fragment

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import app.App
import app.R
import app.adapters.albums.AlbumItem
import app.adapters.albums.ArtistAlbumItem
import app.adapters.artists.ArtistItem
import app.adapters.artists.ArtistsAdapter
import app.events.ui.FilterLocalMusicCommand
import app.helpers.CollectionManager
import app.models.Artist
import app.rx.RxBus
import app.ui.base.BaseFragment
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.InjectView
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = R.layout.fragment_list_artists, injectAllViews = true)
class ArtistsListFragment extends BaseFragment implements SortableFragment {

    LinearLayout empty
    RecyclerView twoWayView
    ProgressBar progress

    @Inject
    protected CollectionManager collectionManager

    private List<ArtistItem> artists = new ArrayList<>()
    private List<ArtistItem> filteredArtists = new ArrayList<>()

    private ArtistsAdapter adapter

    private int sortActionId
    private boolean orderAscending

    Map<Integer, Comparator<ArtistItem>> sortComparators = [
            (R.id.artists_sort_name)        : { ArtistItem l, ArtistItem r -> l.artist.name.compareTo(r.artist.name) } as Comparator<ArtistItem>,
            (R.id.artists_sort_recent_album): { ArtistItem l, ArtistItem r ->
                l.artist.albums.sort { it.year }.last().year.compareTo(r.artist.albums.sort { it.year }.last().year)
            } as Comparator<AlbumItem>,
    ]

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        App.get().inject(this)

        RxBus.on(FilterLocalMusicCommand)
                .bindToLifecycle(this)
                .subscribe(this.&onFilter)
    }

    @Override
    void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)
        twoWayView.layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        adapter = new ArtistsAdapter(artists)
        twoWayView.adapter = adapter
        twoWayView.hasFixedSize = true

        loadArtists()
    }

    private loadArtists() {
        twoWayView.hide()
        progress.show()
        onArtistsLoaded collectionManager.artists
    }

    private void onArtistsLoaded(List<Artist> artists) {
        progress.hide()

        this.artists.clear()
        this.artists.addAll artists.collect {
            def item = new ArtistItem(it)
            item.setSubItems collectionManager.getAlbums(it).collect { new ArtistAlbumItem(it) }
            item
        }

        filteredArtists = new ArrayList(this.artists)

        if (artists) {
            adapter.notifyDataSetChanged()
            twoWayView.show()
        } else {
            empty.show()
        }
    }

    private void onFilter(FilterLocalMusicCommand cmd) {
        adapter.searchText = cmd.constraint
        adapter.filterItems(filteredArtists)
    }

    @Override
    int getSortMenuId() {
        return R.menu.sort_artists
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
        return orderAscending ? R.drawable.sort_ascending : R.drawable.sort_descending
    }

    @Override
    void sortItems() {
        artists.sort true, sortComparators[sortActionId]
        if (!orderAscending)
            artists.reverse true

        filteredArtists = new ArrayList<>(artists)
        adapter.notifyDataSetChanged()
    }
}