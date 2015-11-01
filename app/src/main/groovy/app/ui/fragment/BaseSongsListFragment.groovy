package app.ui.fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import app.R
import app.adapter.SongsListAdapter
import app.commands.SetQueueAndPlayCommand
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.events.ui.ChangeFabActionCommand
import app.events.ui.ShouldShuffleSongsEvent
import app.model.Song
import app.player.LocalPlayer
import app.ui.base.DaggerOttoOnResumeFragment
import app.ui.custom_view.FastScroller
import com.github.s0nerik.betterknife.annotations.InjectView
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import rx.Observable

import javax.inject.Inject

@CompileStatic
abstract class BaseSongsListFragment extends DaggerOttoOnResumeFragment {

    @InjectView(R.id.twoWayView)
    RecyclerView twoWayView
    @InjectView(R.id.fast_scroller)
    FastScroller fastScroller
    @InjectView(R.id.emptyView)
    View emptyView
    @InjectView(R.id.progress)
    View progress

    @PackageScope
    @Inject
    LocalPlayer player

    List<Song> songs = new ArrayList<>()
    Song currentSong

    SongsListAdapter adapter

    private LinearLayoutManager layoutManager

    protected abstract Observable<List<Song>> loadSongs()

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setBusListeners new BusListener(), this
        adapter = new SongsListAdapter(activity, songs)
    }

    @Override
    void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)

        twoWayView.adapter = adapter
        layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        twoWayView.layoutManager = layoutManager
        twoWayView.hasFixedSize = true

        fastScroller.hide()
        twoWayView.hide()
        progress.show()
        loadSongs().subscribe this.&onSongsLoaded
    }

    @Override
    void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {
            bus.post new ChangeFabActionCommand(R.drawable.ic_shuffle_white_24dp, {
                bus.post new SetQueueAndPlayCommand(songs, 0, true)
            })
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

    protected void shuffleAll() {
        if (songs) {
            bus.post new SetQueueAndPlayCommand(songs, 0, true)
        } else {
            Toast.makeText(activity, R.string.nothing_to_shuffle, Toast.LENGTH_LONG).show()
        }
    }

    protected void onSongsLoaded(List<Song> loadedSongs) {
        songs.clear()
        songs.addAll loadedSongs

        progress.hide()
        updateSongsList()
    }

    private void updateSongsList() {
        if (songs) {
            adapter.notifyDataSetChanged()
            selection = currentSong

            twoWayView.show()

            fastScroller.recyclerView = twoWayView
            fastScroller.show()
        } else {
            emptyView.show()
            fastScroller.hide()
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
            adapter.updateEqualizerState(true)
        }

        @Subscribe
        public void onSongPlaybackPaused(PlaybackPausedEvent event) {
            adapter.updateEqualizerState(false)
        }

        @Subscribe
        public void onShuffleSongs(ShouldShuffleSongsEvent event) {
            shuffleAll()
        }

    }

}