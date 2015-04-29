package app.ui.fragment
import android.os.Bundle
import android.support.annotation.Nullable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import app.R
import app.ast.InjectView
import app.ui.async.MusicLoaderService
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.OnClick
import com.joanzapata.android.asyncservice.api.annotation.InjectService
import com.joanzapata.android.asyncservice.api.annotation.OnMessage
import com.joanzapata.android.asyncservice.api.internal.AsyncService
import com.melnykov.fab.FloatingActionButton
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public final class SongsListFragment extends BaseSongsListFragment {

    @InjectView(R.id.fab)
    FloatingActionButton mFab
    @InjectView(R.id.progress)
    ProgressBar progress

    @InjectService
    MusicLoaderService musicLoaderService;

    SongsListFragment() {
        AsyncService.inject(this);
    }

    @Override
    View onCreateView(LayoutInflater inflater,
                      @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        def v = inflater.inflate(R.layout.fragment_list_songs, container, false)
        SwissKnife.inject(this, v)
        return v
    }

    @Override
    protected void loadSongs() {
        progress.setVisibility(View.VISIBLE);
        mFab.hide();
        musicLoaderService.loadAllSongs();
    }

    @OnClick(R.id.fab)
    public void onFabClicked() {
        shuffleAll();
    }

    @OnMessage
    public void onSongsLoaded(MusicLoaderService.SongsLoadedEvent event) {
        progress.setVisibility(View.GONE);
        songs = event.getSongs();
        if (!songs.isEmpty()) {
            initAdapter(songs);
            setSelection(currentSong);
            mFab.show(true);
            mFab.attachToRecyclerView(recycler);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

}