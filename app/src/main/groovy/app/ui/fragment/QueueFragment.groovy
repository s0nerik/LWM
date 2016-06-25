package app.ui.fragment

import android.os.Bundle
import app.App
import app.R
import app.events.player.queue.PlaylistAddedToQueueEvent
import app.events.player.queue.QueueShuffledEvent
import app.events.player.queue.SongAddedToQueueEvent
import app.events.player.queue.SongRemovedFromQueueEvent
import app.models.Song
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.squareup.otto.Subscribe
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

    @Subscribe
    public void onPlaylistAddedToQueueEvent(PlaylistAddedToQueueEvent event) {
        int startIndex = songs.size()
//        songs.addAll event.appendedSongs

        adapter.notifyItemRangeInserted startIndex, event.appendedSongs.size()
    }

    @Subscribe
    public void onQueueShuffled(QueueShuffledEvent event) {
        songs.clear()
//        songs.addAll event.queue
        adapter.notifyDataSetChanged()
    }

    @Subscribe
    public void onSongAddedToQueue(SongAddedToQueueEvent event) {
//        songs.add event.song
        adapter.notifyItemInserted songs.size() - 1
    }

    @Subscribe
    public void onSongRemovedFromQueue(SongRemovedFromQueueEvent event) {
        songs.remove event.song
        adapter.notifyItemRemoved songs.size()
    }

    @Override
    protected Observable<List<Song>> loadSongs() {
        Observable.just(player.queue)
    }
}