package app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.View
import android.widget.*
import app.R
import app.adapter.AlbumsAdapter
import app.data_managers.AlbumsManager
import app.model.Album
import app.model.Artist
import app.ui.activity.AlbumInfoActivity
import app.ui.base.DaggerOttoOnResumeFragment
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnItemClick
import groovy.transform.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
@InjectLayout(value = R.layout.fragment_list_albums, injectAllViews = true)
public class AlbumsListFragment extends DaggerOttoOnResumeFragment {

    GridView grid
    LinearLayout empty
    ProgressBar progress

    @Inject
    AlbumsManager albumsManager

    Artist artist

    private List<Album> albums = new ArrayList<>();

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
        grid.adapter = new AlbumsAdapter(activity, albums)

        loadAlbums()
    }

    private void loadAlbums() {
        grid.hide()
        progress.show()

        Observable<Album> observable

        if (artist) {
            observable = albumsManager.loadAllAlbums(artist)
        } else {
            observable = albumsManager.loadAllAlbums()
        }

        // TODO: replace this with caching of supported songs inside SongsManager
        observable
//                .concatMap {
//                    def songsNum = it.songs.count().toBlocking().singleOrDefault(0)
//
//                    if (songsNum > 0) Observable.just it
//                    else Observable.empty()
//                }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe this.&onAlbumsLoaded
    }

    private void onAlbumsLoaded(List<Album> loadedAlbums) {
        progress.hide()

        albums.clear()
        albums.addAll loadedAlbums

        if (albums) {
            grid.show()
        } else {
            grid.hide()
            empty.show()
        }
    }

    @OnItemClick(R.id.grid)
    public void onItemClick(int position) {
        def intent = new Intent(activity, AlbumInfoActivity)
        intent.putExtra "album", albums[position] as Parcelable
        startActivity intent
        activity.overridePendingTransition R.anim.slide_in_right, R.anim.slide_out_left_long_alpha
    }
}
