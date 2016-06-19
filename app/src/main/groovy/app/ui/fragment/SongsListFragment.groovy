package app.ui.fragment
import app.R
import app.helpers.CollectionManager
import app.models.Song
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import rx.Observable

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = R.layout.fragment_list_songs, injectAllViews = true)
final class SongsListFragment extends BaseSongsListFragment {

    @Inject
    @PackageScope
    CollectionManager collectionManager

    @Inject
    @PackageScope
    Bus bus

    @Override
    protected Observable<List<Song>> loadSongs() {
//        def waitingDialog = new MaterialDialog.Builder(activity).title("Updating your collection...").build()
        Observable.just(collectionManager.songs)
    }

}