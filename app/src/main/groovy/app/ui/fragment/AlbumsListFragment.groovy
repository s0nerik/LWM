package app.ui.fragment

import android.os.Bundle
import android.os.Parcelable
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
import app.helpers.providers.SorterProviders
import app.models.Album
import app.models.Artist
import app.ui.base.BaseFragment
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.rxbus.RxBus
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = R.layout.fragment_list_albums, injectAllViews = true)
class AlbumsListFragment extends BaseFragment implements SortableFragment {

    RecyclerView recycler
    LinearLayout empty
    ProgressBar progress

    @Inject
    protected CollectionManager collectionManager

    Artist artist

    private List<AlbumItem> albums = new ArrayList<>()
    private List<AlbumItem> filteredAlbums = new ArrayList<>()

    private AlbumsAdapter adapter

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

        RxBus.on(FilterLocalMusicCommand)
                .bindToLifecycle(this)
                .subscribe(this.&onFilter)
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

    private void onFilter(FilterLocalMusicCommand cmd) {
        adapter.searchText = cmd.constraint
        adapter.filterItems(filteredAlbums)
    }

    @Override
    int getSortMenuId() { R.menu.sort_albums }

    @Override
    int getDefaultSortActionId() { R.id.albums_sort_title }

    @Override
    RecyclerView.Adapter getAdapter() {
        adapter
    }

    @Override
    List<AlbumItem> getSortableList() {
        albums
    }

    @Override
    Map<Integer, Closure> getSorters() {
        SorterProviders.ALBUMS
    }
}
