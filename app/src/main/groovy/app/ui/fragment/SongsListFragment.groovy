package app.ui.fragment
import android.view.View
import android.widget.ProgressBar
import app.R
import app.data_managers.SongsManager
import app.model.Song
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnClick
import com.melnykov.fab.FloatingActionButton
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
@InjectLayout(value = R.layout.fragment_list_songs, injectAllViews = true)
public final class SongsListFragment extends BaseSongsListFragment {

    FloatingActionButton fab
    ProgressBar progress

    @Inject
    SongsManager songsManager

    @Override
    protected void loadSongs() {
        progress.visibility = View.VISIBLE
        fastScroller.visibility = View.GONE
        fab.hide()
        songsManager.loadAllSongs().subscribe this.&onSongsLoaded
    }

    @OnClick(R.id.fab)
    public void onFabClicked() {
        shuffleAll()
    }

    private void onSongsLoaded(List<Song> songs) {
        progress.visibility = View.GONE
        this.songs = songs
        if (songs) {
            initAdapter songs
            selection = currentSong
            fab.show true
            fab.attachToRecyclerView twoWayView
            fastScroller.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.VISIBLE
            fastScroller.visibility = View.GONE
        }
    }

}