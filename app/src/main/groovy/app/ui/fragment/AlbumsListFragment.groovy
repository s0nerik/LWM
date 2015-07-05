package app.ui.fragment
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ProgressBar
import app.R
import app.adapter.AlbumsAdapter
import app.model.Album
import app.model.Artist
import app.ui.activity.AlbumInfoActivity
import app.data_managers.SongsManager
import app.ui.base.DaggerOttoOnResumeFragment
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnItemClick
import com.joanzapata.android.asyncservice.api.annotation.OnMessage
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class AlbumsListFragment extends DaggerOttoOnResumeFragment {

    @InjectView(R.id.grid)
    GridView mGrid;
    @InjectView(android.R.id.empty)
    LinearLayout mEmpty;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

//    @InjectService
//    SongsManager musicLoaderService;

    Artist artist

    public static Fragment create(Artist artist) {
        def fragment = new AlbumsListFragment()
        def bundle = new Bundle()
        bundle.putParcelable("artist", artist as Parcelable)
        fragment.setArguments(bundle)
        fragment
    }

    private List<Album> albums = new ArrayList<>();

//    public AlbumsListFragment() {
//        AsyncService.inject(this);
//    }

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        artist = getArguments()?.getParcelable("artist") as Artist
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_albums, container, false);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgress.setVisibility(View.VISIBLE);
//        musicLoaderService.loadAllAlbums(artist);
    }

    @OnMessage
    public void onAlbumsLoaded(SongsManager.AlbumsLoadedEvent event) {
        mProgress.setVisibility(View.GONE);
        albums = event.getAlbums();
        initAdapter(albums);
    }

    @OnItemClick(R.id.grid)
    public void onItemClick(int position) {
        def intent = new Intent(activity, AlbumInfoActivity)
        intent.putExtra "album", albums.get(position) as Parcelable
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left_long_alpha);
    }

    private void initAdapter(List<Album> albums) {
        ListAdapter adapter = new AlbumsAdapter(getActivity(), albums);

        if (adapter.getCount() > 0) {
            mGrid.setVisibility(View.VISIBLE);
            mGrid.setAdapter(adapter);
        } else {
            mGrid.setVisibility(View.GONE);
            mEmpty.setVisibility(View.VISIBLE);
        }
    }
}
