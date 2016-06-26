package app.ui.fragment

import android.os.Bundle
import app.App
import app.R
import app.events.player.queue.PlaylistAddedToQueueEvent
import app.events.player.queue.QueueShuffledEvent
import app.events.player.queue.SongAddedToQueueEvent
import app.events.player.queue.SongRemovedFromQueueEvent
import app.models.Song
import com.github.s0nerik.rxbus.RxBus
import com.github.s0nerik.betterknife.annotations.InjectLayout
import groovy.transform.CompileStatic
import rx.Observable

@CompileStatic
@InjectLayout(R.layout.fragment_list_queue)
final class QueueFragment extends BaseSongsListFragment {

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        App.get().inject(this)
    }

    @Override
    protected void initEventHandlersOnCreate() {
        RxBus.on(PlaylistAddedToQueueEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(QueueShuffledEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(SongAddedToQueueEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(SongRemovedFromQueueEvent).bindToLifecycle(this).subscribe(this.&onEvent)
    }

    // region Event handlers

    private void onEvent(PlaylistAddedToQueueEvent event) {
        int startIndex = songs.size()
//        songs.addAll event.appendedSongs

        adapter.notifyItemRangeInserted startIndex, event.appendedSongs.size()
    }

    private void onEvent(QueueShuffledEvent event) {
        songs.clear()
//        songs.addAll event.queue
        adapter.notifyDataSetChanged()
    }

    private void onEvent(SongAddedToQueueEvent event) {
//        songs.add event.song
        adapter.notifyItemInserted songs.size() - 1
    }

    private void onEvent(SongRemovedFromQueueEvent event) {
        songs.remove event.song
        adapter.notifyItemRemoved songs.size()
    }

    // endregion

    @Override
    protected Observable<List<Song>> loadSongs() {
        Observable.just(player.queue)
    }
}