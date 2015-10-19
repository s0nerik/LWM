package app.ui.fragment

import app.R
import app.data_managers.SongsManager
import app.model.Song
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = R.layout.fragment_list_songs, injectAllViews = true)
final class SongsListFragment extends BaseSongsListFragment {

    @Inject
    @PackageScope
    SongsManager songsManager

    @Inject
    @PackageScope
    Bus bus

    @Override
    protected Observable<List<Song>> loadSongs() {
        songsManager.loadAllSongs()
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

}