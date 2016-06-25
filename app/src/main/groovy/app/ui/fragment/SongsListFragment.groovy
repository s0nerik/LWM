package app.ui.fragment

import android.os.Bundle
import app.App
import app.R
import app.helpers.CollectionManager
import app.models.Song
import com.github.s0nerik.betterknife.annotations.InjectLayout
import groovy.transform.CompileStatic
import rx.Observable

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = R.layout.fragment_list_songs, injectAllViews = true)
final class SongsListFragment extends BaseSongsListFragment {

    @Inject
    protected CollectionManager collectionManager

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        App.get().inject(this)
    }

    @Override
    protected Observable<List<Song>> loadSongs() {
//        def waitingDialog = new MaterialDialog.Builder(activity).title("Updating your collection...").build()
        Observable.just(collectionManager.songs)
    }

}