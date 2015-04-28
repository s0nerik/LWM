package app.ui.fragment

import android.view.View
import app.ui.async.MusicLoaderService
import com.arasthel.swissknife.annotations.InjectView
import com.arasthel.swissknife.annotations.OnClick
import com.joanzapata.android.asyncservice.api.annotation.InjectService
import com.joanzapata.android.asyncservice.api.annotation.OnMessage
import com.joanzapata.android.asyncservice.api.internal.AsyncService
import app.R
import com.melnykov.fab.FloatingActionButton
import groovy.transform.CompileStatic

@CompileStatic
public class SongsListFragment extends BaseSongsListFragment {

    @InjectView(R.id.fab)
    FloatingActionButton mFab;

    @InjectService
    MusicLoaderService musicLoaderService;

    public SongsListFragment() {
        AsyncService.inject(this);
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_list_songs;
    }

    @Override
    protected void loadSongs() {
        mProgress.setVisibility(View.VISIBLE);
        mFab.hide();
        musicLoaderService.loadAllSongs();
    }

    @OnClick(R.id.fab)
    public void onFabClicked() {
        shuffleAll();
    }

    @OnMessage
    public void onSongsLoaded(MusicLoaderService.SongsLoadedEvent event) {
        mProgress.setVisibility(View.GONE);
        songs = event.getSongs();
        if (!songs.isEmpty()) {
            initAdapter(songs);
            setSelection(currentSong);
            mFab.show(true);
            mFab.attachToRecyclerView(mTwoWayView);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

}