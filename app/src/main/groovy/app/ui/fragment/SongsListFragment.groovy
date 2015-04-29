package app.ui.fragment

import android.view.View
import android.widget.ProgressBar
import app.R
import app.ui.async.MusicLoaderService
import com.arasthel.swissknife.annotations.OnClick
import com.joanzapata.android.asyncservice.api.annotation.InjectService
import com.joanzapata.android.asyncservice.api.annotation.OnMessage
import com.joanzapata.android.asyncservice.api.internal.AsyncService
import com.melnykov.fab.FloatingActionButton
import fr.grousset.fastsnail.transform.InjectLayout
import fr.grousset.fastsnail.transform.InjectView
import groovy.transform.CompileStatic

@CompileStatic
@InjectLayout(R.layout.fragment_list_songs)
public class SongsListFragment extends BaseSongsListFragment {

    @InjectView(R.id.fab)
    FloatingActionButton mFab;
    @InjectView(R.id.emptyView)
    View emptyView
    @InjectView(R.id.progress)
    ProgressBar progress

    @InjectService
    MusicLoaderService musicLoaderService;

    SongsListFragment() {
        AsyncService.inject(this);
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