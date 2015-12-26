package app.ui.fragment

import app.R
import app.data_managers.CollectionManager
import app.data_managers.SongsManager
import app.model.Song
import com.afollestad.materialdialogs.MaterialDialog
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

    private Observable<List<Song>> songsObservable

    @Override
    protected Observable<List<Song>> loadSongs() {
        def waitingDialog = new MaterialDialog.Builder(activity).title("Updating your collection...").build()

        songsObservable =
                CollectionManager
                        .initFromFile()
                        .onErrorResumeNext(
                            CollectionManager.initFromMediaStore()
                                             .doOnSubscribe {
                                                 activity.runOnUiThread {
                                                     waitingDialog.show()
                                                 }
                                             }
                                             .doOnCompleted {
                                                 activity.runOnUiThread {
                                                     waitingDialog.hide()
                                                 }
                                             })
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())

        return songsObservable
    }

}