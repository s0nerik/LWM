package app.ui.fragment

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import app.R
import app.adapter.songs.SongItem
import app.adapter.songs.SongsListAdapter
import app.commands.RequestPlaySongCommand
import app.commands.SetQueueAndPlayCommand
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.events.ui.ChangeFabActionCommand
import app.events.ui.FilterLocalMusicCommand
import app.events.ui.ShouldShuffleSongsEvent
import app.model.Song
import app.player.LocalPlayer
import app.ui.base.DaggerOttoOnResumeFragment
import com.github.s0nerik.betterknife.annotations.InjectView
import com.squareup.otto.Subscribe
import eu.davidea.fastscroller.FastScroller
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import rx.Observable

import javax.inject.Inject

@CompileStatic
abstract class BaseSongsListFragment extends DaggerOttoOnResumeFragment implements SortableFragment {

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

    List<SongItem> songs = new ArrayList<>()
    List<SongItem> filteredSongs = new ArrayList<>()
    protected Song currentSong

    SongsListAdapter adapter

    private int sortActionId
    private boolean orderAscending

    Map<Integer, Closure> sortFieldProviders = [
            (R.id.songs_sort_title): { SongItem it -> it.song.title },
            (R.id.songs_sort_artist): { SongItem it -> it.song.artistName },
            (R.id.songs_sort_album): { SongItem it -> it.song.albumName },
            (R.id.songs_sort_year): { SongItem it -> it.song?.album?.year },
    ]

    Map<Integer, Closure<String>> bubbleTextProviders = [
            (R.id.songs_sort_title): { SongItem it -> it.song.title[0] },
            (R.id.songs_sort_artist): { SongItem it -> it.song.artistName[0] },
            (R.id.songs_sort_album): { SongItem it -> it.song.albumName[0] },
            (R.id.songs_sort_year): { SongItem it -> (it.song?.album?.year as String)[0..3] },
    ]

    private LinearLayoutManager layoutManager

    protected abstract Observable<List<Song>> loadSongs()

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setBusListeners new BusListener(), this

        adapter = new SongsListAdapter(songs)
        adapter.mode = SongsListAdapter.MODE_SINGLE
    }

    @Override
    void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)

        twoWayView.adapter = adapter
        layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        twoWayView.layoutManager = layoutManager
        twoWayView.hasFixedSize = true

        adapter.setFastScroller fastScroller, resources.getColor(R.color.md_deep_purple_600)

//        fastScroller.hide()
        twoWayView.hide()
        progress.show()
        loadSongs().subscribe this.&onSongsLoaded
    }

    @Override
    void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {
            bus.post new ChangeFabActionCommand(R.drawable.ic_shuffle_white_24dp, this.&shuffleAll)
        }
    }

    protected void shuffleAll() {
        if (filteredSongs) {
            bus.post new SetQueueAndPlayCommand(filteredSongs.collect {it.song}, 0, true)
        } else {
            Toast.makeText(activity, R.string.nothing_to_shuffle, Toast.LENGTH_LONG).show()
        }
    }

    protected void onSongsLoaded(List<Song> loadedSongs) {
        songs.clear()
        songs.addAll loadedSongs.collect { new SongItem(it) }

        filteredSongs = new ArrayList(songs)

        progress.hide()
        updateSongsList()
    }

    private void updateSongsList() {
        if (songs) {
            adapter.notifyDataSetChanged()
            twoWayView.show()
        } else {
            emptyView.show()
        }
    }

    void setCurrentSong(Song song) {
        this.@currentSong = song
        adapter.toggleSelection(filteredSongs.collect {it.song}.indexOf(currentSong))
    }

    private class BusListener {
        @Subscribe
        public void onCurrentSongAvailable(CurrentSongAvailableEvent event) {
            currentSong = event.song
        }

        @Subscribe
        public void onSongPlaybackStarted(PlaybackStartedEvent event) {
            currentSong = event.song
        }

        @Subscribe
        public void onSongPlaybackPaused(PlaybackPausedEvent event) {
//            adapter.updateEqualizerState(false)
        }

        @Subscribe
        public void onShuffleSongs(ShouldShuffleSongsEvent event) {
            shuffleAll()
        }

        @Subscribe
        public void onEvent(FilterLocalMusicCommand cmd) {
            adapter.searchText = cmd.constraint
            adapter.filterItems(filteredSongs)
        }

        @Subscribe
        public void onEvent(RequestPlaySongCommand cmd) {
            def queue = filteredSongs.collect { it.song }
            bus.post new SetQueueAndPlayCommand(queue, queue.indexOf(cmd.song))
        }
    }

    @Override
    int getSortMenuId() {
        return R.menu.sort_songs
    }

    @Override
    int getSortActionId() {
        return sortActionId
    }

    @Override
    void setSortActionId(@IdRes int id) {
        sortActionId = id
    }

    @Override
    boolean isOrderAscending() {
        return orderAscending
    }

    @Override
    void setOrderAscending(boolean value) {
        orderAscending = value
    }

    @Override
    int getSortIconId() {
        return orderAscending ? R.drawable.sort_ascending : R.drawable.sort_descending
    }

    @Override
    void sortItems() {
        adapter.bubbleTextProvider = bubbleTextProviders[sortActionId]

        songs.sort true, sortFieldProviders[sortActionId]
        if (!orderAscending)
            songs.reverse true

        filteredSongs = new ArrayList<>(songs)
        adapter.notifyDataSetChanged()
    }
}