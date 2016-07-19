package app.ui.fragment

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import app.R
import app.adapters.songs.SongItem
import app.adapters.songs.SongsListAdapter
import app.commands.RequestPlaySongCommand
import app.commands.SetQueueAndPlayCommand
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.events.ui.ChangeFabActionCommand
import app.events.ui.FilterLocalMusicCommand
import app.events.ui.ShouldShuffleSongsEvent
import app.helpers.providers.BubbleTextProviders
import app.helpers.providers.SorterProviders
import app.models.Song
import app.players.LocalPlayer
import app.ui.base.BaseFragment
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.rxbus.RxBus
import eu.davidea.fastscroller.FastScroller
import groovy.transform.CompileStatic
import rx.Observable

import javax.inject.Inject

@CompileStatic
abstract class BaseSongsListFragment extends BaseFragment implements SortableFragment {

    @InjectView(R.id.twoWayView)
    RecyclerView twoWayView
    @InjectView(R.id.fast_scroller)
    FastScroller fastScroller
    @InjectView(R.id.emptyView)
    View emptyView
    @InjectView(R.id.progress)
    View progress

    @Inject
    protected LocalPlayer player

    List<SongItem> songs = new ArrayList<>()
    List<SongItem> filteredSongs = new ArrayList<>()
    protected Song currentSong

    protected SongsListAdapter adapter

    private int sortActionId
    private boolean orderAscending

    protected abstract Observable<List<Song>> loadSongs()

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        adapter = new SongsListAdapter(songs)
        adapter.mode = SongsListAdapter.MODE_SINGLE

        sortActionId = R.id.songs_sort_title

        RxBus.on(CurrentSongAvailableEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(PlaybackStartedEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(PlaybackPausedEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(ShouldShuffleSongsEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(FilterLocalMusicCommand).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(RequestPlaySongCommand).bindToLifecycle(this).subscribe(this.&onEvent)
    }

    @Override
    void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)

        twoWayView.adapter = adapter
        twoWayView.hasFixedSize = true

        adapter.setFastScroller fastScroller, resources.getColor(R.color.md_deep_purple_600)

//        fastScroller.hide()
        twoWayView.hide()
        progress.show()
        loadSongs().subscribe this.&onSongsLoaded, { log("Error loading songs" ,it) }
    }

    @Override
    void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {
            RxBus.post new ChangeFabActionCommand(R.drawable.ic_shuffle_white_24dp, this.&shuffleAll)
        }
    }

    protected void shuffleAll() {
        if (filteredSongs) {
            RxBus.post new SetQueueAndPlayCommand(filteredSongs.collect {it.song}, 0, true)
        } else {
            Toast.makeText(activity, R.string.nothing_to_shuffle, Toast.LENGTH_LONG).show()
        }
    }

    protected void onSongsLoaded(List<Song> loadedSongs) {
        songs.clear()
        songs.addAll loadedSongs.collect { new SongItem(it) }

        sortItems()

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

    // region Event handlers

    protected void onEvent(CurrentSongAvailableEvent event) {
        currentSong = event.song
    }

    protected void onEvent(PlaybackStartedEvent event) {
        currentSong = event.song
    }

    protected void onEvent(PlaybackPausedEvent event) {
//            adapter.updateEqualizerState(false)
    }

    protected void onEvent(ShouldShuffleSongsEvent event) {
        shuffleAll()
    }

    protected void onEvent(FilterLocalMusicCommand cmd) {
        adapter.searchText = cmd.constraint
        adapter.filterItems(filteredSongs)
    }

    protected void onEvent(RequestPlaySongCommand cmd) {
        def queue = filteredSongs.collect { it.song }
        RxBus.post new SetQueueAndPlayCommand(queue, queue.indexOf(cmd.song))
    }

    // endregion

    @Override
    RecyclerView.Adapter getAdapter() { adapter }

    @Override
    Map<Integer, Closure> getSorters() { SorterProviders.SONGS }

    @Override
    List<SongItem> getSortableList() { songs }

    @Override
    int getSortMenuId() { R.menu.sort_songs }

    @Override
    int getDefaultSortActionId() { R.id.songs_sort_title }

    @Override
    Map<Integer, Closure<String>> getSortingBubbleTextProviders() { BubbleTextProviders.SONGS }
}