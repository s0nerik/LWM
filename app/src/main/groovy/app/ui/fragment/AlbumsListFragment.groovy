package app.ui.fragment
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.util.Pair
import android.view.View
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ProgressBar
import app.R
import app.adapter.AlbumsAdapter
import app.data_managers.AlbumsManager
import app.model.Album
import app.model.Artist
import app.ui.activity.AlbumInfoActivity
import app.ui.base.DaggerOttoOnResumeFragment
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnItemClick
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

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

    public static Fragment create(Artist artist) {
        def fragment = new AlbumsListFragment()
        def bundle = new Bundle()
        bundle.putParcelable "artist", artist as Parcelable
        fragment.arguments = bundle
        fragment
    }

    private List<Album> albums = new ArrayList<>();

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        artist = arguments?.getParcelable("artist") as Artist
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)
        progress.visibility = View.VISIBLE

        albumsManager.loadAllAlbums(artist).subscribe this.&onAlbumsLoaded
    }

    private void onAlbumsLoaded(Pair<Artist, List<Album>> albums) {
        progress.visibility = View.GONE
        this.albums = albums.second
        initAdapter albums.second
    }

    @OnItemClick(R.id.grid)
    public void onItemClick(int position) {
        def intent = new Intent(activity, AlbumInfoActivity)
        intent.putExtra "album", albums.get(position) as Parcelable
        startActivity intent
        activity.overridePendingTransition R.anim.slide_in_right, R.anim.slide_out_left_long_alpha
    }

    private void initAdapter(List<Album> albums) {
        ListAdapter adapter = new AlbumsAdapter(activity, albums)

        if (adapter.count > 0) {
            grid.visibility = View.VISIBLE
            grid.adapter = adapter
        } else {
            grid.visibility = View.GONE
            empty.visibility = View.VISIBLE
        }
    }
}
