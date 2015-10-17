package app.ui.fragment

import android.widget.ProgressBar
import app.R
import app.data_managers.SongsManager
import app.model.Song
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnClick
import com.github.s0nerik.betterknife.annotations.Profile

import com.melnykov.fab.FloatingActionButton
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import rx.Observable

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = R.layout.fragment_list_songs, injectAllViews = true)
public final class SongsListFragment extends BaseSongsListFragment {

    FloatingActionButton fab
    ProgressBar progress

    @Inject
    @PackageScope
    SongsManager songsManager

    @Inject
    @PackageScope
    Bus bus

    @OnClick(R.id.fab)
    public void onFabClicked() {
        shuffleAll()
    }

    @Override
    protected Observable<List<Song>> loadSongs() {
        songsManager.loadAllSongs().toList()
    }

}