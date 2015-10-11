package app.ui.fragment

import android.view.View
import app.R
import app.events.player.queue.PlaylistAddedToQueueEvent
import app.events.player.queue.QueueShuffledEvent
import app.events.player.queue.SongAddedToQueueEvent
import app.events.player.queue.SongRemovedFromQueueEvent
import app.model.Song
import app.player.LocalPlayer
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.Profile
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import rx.Observable

import javax.inject.Inject

@CompileStatic
@InjectLayout(R.layout.fragment_list_queue)
public final class QueueFragment extends BaseSongsListFragment {

    @Inject
    @PackageScope
    LocalPlayer player

    @Inject
    @PackageScope
    Bus bus

    @Subscribe
    public void onPlaylistAddedToQueueEvent(PlaylistAddedToQueueEvent event) {
        int startIndex = songs.size()
        songs.addAll event.appendedSongs

        adapter.notifyItemRangeInserted startIndex, event.appendedSongs.size()
    }

    @Subscribe
    public void onQueueShuffled(QueueShuffledEvent event) {
        songs.clear()
        songs.addAll event.queue
        adapter.notifyDataSetChanged()
    }

    @Subscribe
    public void onSongAddedToQueue(SongAddedToQueueEvent event) {
        songs.add event.song
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