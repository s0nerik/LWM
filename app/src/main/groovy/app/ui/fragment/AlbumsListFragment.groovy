package app.ui.fragment

import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import app.App
import app.R
import app.adapters.albums.AlbumItem
import app.adapters.albums.AlbumsAdapter
import app.events.ui.FilterLocalMusicCommand
import app.helpers.CollectionManager
import app.models.Album
import app.models.Artist
import app.ui.base.OttoOnResumeFragment
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = R.layout.fragment_list_albums, injectAllViews = true)
class AlbumsListFragment extends OttoOnResumeFragment implements SortableFragment {

    RecyclerView recycler
    LinearLayout empty
    ProgressBar progress

    @Inject
    protected CollectionManager collectionManager

    Artist artist

    private List<AlbumItem> albums = new ArrayList<>()
    private List<AlbumItem> filteredAlbums = new ArrayList<>()

    private AlbumsAdapter adapter

    private int sortActionId
    private boolean orderAscending

    Map<Integer, Comparator<AlbumItem>> sortComparators = [
            (R.id.albums_sort_title): { AlbumItem l, AlbumItem r -> l.album.title.compareTo(r.album.title) } as Comparator<AlbumItem>,
            (R.id.albums_sort_artist): { AlbumItem l, AlbumItem r -> l.album.artistName.compareTo(r.album.artistName) } as Comparator<AlbumItem>,
            (R.id.albums_sort_year): { AlbumItem l, AlbumItem r -> l.album.year?.compareTo(r.album.year) } as Comparator<AlbumItem>,
    ]

    public static Fragment create(Artist artist) {
        def fragment = new AlbumsListFragment()
        def bundle = new Bundle()
        bundle.putParcelable "artist", artist as Parcelable
        fragment.arguments = bundle
        fragment
    }

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        App.get().inject(this)
        artist = arguments?.getParcelable("artist") as Artist
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)
        adapter = new AlbumsAdapter(albums)
        recycler.adapter = adapter

        loadAlbums()
    }

    private void loadAlbums() {
        recycler.hide()
        progress.show()

        onAlbumsLoaded(artist ? artist.albums : collectionManager.albums)
    }

    private void onAlbumsLoaded(List<Album> loadedAlbums) {
        progress.hide()

        albums.clear()
        albums.addAll loadedAlbums.collect { new AlbumItem(it) }

        filteredAlbums = new ArrayList(albums)

        if (albums) {
            recycler.show()
        } else {
            recycler.hide()
            empty.show()
        }
    }

    @Subscribe
    void onEvent(FilterLocalMusicCommand cmd) {
        adapter.searchText = cmd.constraint
        adapter.filterItems(filteredAlbums)
    }

    @Override
    int getSortMenuId() {
        return R.menu.sort_albums
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
        albums.sort true, sortComparators[sortActionId]
        if (!orderAscending)
            albums.reverse true

        filteredAlbums = new ArrayList<>(albums)
        adapter.notifyDataSetChanged()
    }
}
