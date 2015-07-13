package app.ui.fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import app.R
import app.adapter.SongsListAdapter
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.events.ui.ShouldShuffleSongsEvent
import app.model.Song
import app.player.LocalPlayer
import app.ui.base.DaggerOttoOnResumeFragment
import app.ui.custom_view.FastScroller

import com.github.s0nerik.betterknife.annotations.InjectView
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
abstract class BaseSongsListFragment extends DaggerOttoOnResumeFragment {

    @InjectView
    RecyclerView twoWayView
    @InjectView(R.id.fast_scroller)
    FastScroller fastScroller
    @InjectView
    View emptyView

    @PackageScope
    @Inject
    LocalPlayer player

    List<Song> songs
    Song currentSong

    SongsListAdapter adapter

    private LinearLayoutManager layoutManager

    protected abstract void loadSongs()

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setBusListeners new BusListener(), this
    }

    @Override
    void onViewCreated(View view, Bundle savedInstanceState) {
        layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        twoWayView.layoutManager = layoutManager
        twoWayView.hasFixedSize = true
        fastScroller.recyclerView = twoWayView
        if (!savedInstanceState) {
            loadSongs()
        }
    }

    protected void setSelection(Song song) {
        if (songs) {
            int index = songs.indexOf song
            if (index >= 0) {
                setSelection(index)
            }
        }
    }

    protected void setSelection(int position) {
        int prevSelection = adapter.selection
        adapter.selection = position
        if (position < layoutManager.findFirstCompletelyVisibleItemPosition() ||
                position > layoutManager.findLastCompletelyVisibleItemPosition()) {
            if (Math.abs(prevSelection - adapter.selection) <= 100) {
                twoWayView.smoothScrollToPosition position
            } else {
                twoWayView.scrollToPosition position
            }
        }
    }

    protected void initAdapter(List<Song> songs) {
        adapter = new SongsListAdapter(activity, songs)
        twoWayView.adapter = adapter
    }

    protected void shuffleAll() {
        if (songs) {
            def queue = new ArrayList<>(songs);
            player.queue = queue
            player.shuffleQueue()
            player.play(0)
        } else {
            Toast.makeText(activity, R.string.nothing_to_shuffle, Toast.LENGTH_LONG).show()
        }
    }

    private class BusListener {
        @Subscribe
        public void onCurrentSongAvailable(CurrentSongAvailableEvent event) {
            currentSong = event.song
            setSelection currentSong
        }

        @Subscribe
        public void onSongPlaybackStarted(PlaybackStartedEvent event) {
            currentSong = event.song
            setSelection currentSong
        }

        @Subscribe
        public void onSongPlaybackPaused(PlaybackPausedEvent event) {
            adapter.updateEqualizerState()
        }

        @Subscribe
        public void onShuffleSongs(ShouldShuffleSongsEvent event) {
            shuffleAll()
        }

    }

}