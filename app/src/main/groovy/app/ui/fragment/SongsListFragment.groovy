package app.ui.fragment

import android.os.Bundle
import android.support.annotation.Nullable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import app.R
import app.data_managers.SongsManager
import app.model.Song
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.joanzapata.android.asyncservice.api.internal.AsyncService
import com.melnykov.fab.FloatingActionButton
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public final class SongsListFragment extends BaseSongsListFragment {

    @InjectView(R.id.fab)
    FloatingActionButton mFab
    @InjectView(R.id.progress)
    ProgressBar progress

    @Inject
    SongsManager songsManager;

    SongsListFragment() {
        AsyncService.inject(this);
    }

    @Override
    View onCreateView(LayoutInflater inflater,
                      @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        def v = inflater.inflate(R.layout.fragment_list_songs, container, false)
        return v
    }

    @Override
    protected void loadSongs() {
        progress.setVisibility(View.VISIBLE);
        mFab.hide();
        songsManager.loadAllSongs().subscribe(this.&onSongsLoaded);
    }

    @OnClick(R.id.fab)
    public void onFabClicked() {
        shuffleAll();
    }

    private void onSongsLoaded(List<Song> songs) {
        progress.setVisibility(View.GONE);
        if (!songs.isEmpty()) {
            initAdapter(songs);
            setSelection(currentSong);
            mFab.show(true);
            mFab.attachToRecyclerView(recycler);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

//    @OnMessage
//    public void onSongsLoaded(SongsManager.SongsLoadedEvent event) {
//        progress.setVisibility(View.GONE);
//        songs = event.getSongs();
//        if (!songs.isEmpty()) {
//            initAdapter(songs);
//            setSelection(currentSong);
//            mFab.show(true);
//            mFab.attachToRecyclerView(recycler);
//        } else {
//            emptyView.setVisibility(View.VISIBLE);
//        }
//    }

}