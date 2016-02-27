package app.ui.fragment

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import app.R
import app.adapter.albums.AlbumItem
import app.adapter.albums.AlbumsAdapter
import app.helper.CollectionManager
import app.model.Album
import app.model.Artist
import app.ui.base.DaggerOttoOnResumeFragment
import com.github.s0nerik.betterknife.annotations.InjectLayout
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
@InjectLayout(value = R.layout.fragment_list_albums, injectAllViews = true)
public class AlbumsListFragment extends DaggerOttoOnResumeFragment {

    RecyclerView recycler
    LinearLayout empty
    ProgressBar progress

    @Inject
    CollectionManager collectionManager

    Artist artist

    private List<AlbumItem> albums = new ArrayList<>();

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
        artist = arguments?.getParcelable("artist") as Artist
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)
        recycler.adapter = new AlbumsAdapter(albums)

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

        if (albums) {
            recycler.show()
        } else {
            recycler.hide()
            empty.show()
        }
    }
}
