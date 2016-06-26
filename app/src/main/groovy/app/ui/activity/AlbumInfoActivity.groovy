package app.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import app.R
import app.Utils
import app.adapters.songs.SongItem
import app.adapters.songs.SongsListAdapter
import app.commands.EnqueueCommand
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.models.Album
import app.models.Song
import com.github.s0nerik.rxbus.RxBus
import app.ui.fragment.NowPlayingFragment
import com.bumptech.glide.Glide
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.Extra
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.jakewharton.rxbinding.support.design.widget.RxAppBarLayout
import groovy.transform.CompileStatic
import ru.noties.debug.Debug
import rx.subscriptions.CompositeSubscription

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = R.layout.activity_album_info, injectAllViews = true)
class AlbumInfoActivity extends BaseLocalActivity {

    private List<Song> songs

    Toolbar toolbar
    AppBarLayout appBarLayout
    TextView title
    TextView subtitle
    ImageView image
    RecyclerView recycler
    NowPlayingFragment nowPlayingFragment
    CoordinatorLayout coordinator
    FloatingActionButton fab

    @Inject
    protected LayoutInflater inflater

    @Inject
    protected Utils utils

    @Extra
    Album album

    private CompositeSubscription scrollSubscribers

    private LinearLayoutManager layoutManager
    private RecyclerView.OnScrollListener recyclerScrollListener = new RecyclerView.OnScrollListener() {
        private int lastDy = 0

        @Override
        void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && lastDy < 0
                    && layoutManager.findFirstCompletelyVisibleItemPosition() == 0 ) {
                appBarLayout.setExpanded(true, true)
            }
        }

        @Override
        void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            lastDy = dy
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState)
        BetterKnife.loadExtras this

        songs = album.songs

//        def adapter = new SongsListAdapter(this, songs)
        def adapter = new SongsListAdapter(songs.collect { new SongItem(it) })
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        recycler.adapter = adapter
        recycler.layoutManager = this.layoutManager
        recycler.hasFixedSize = true

        subscribeToScrollEvents()

        nowPlayingFragment = supportFragmentManager.findFragmentById(R.id.nowPlayingFragment) as NowPlayingFragment
        supportFragmentManager.beginTransaction().hide(nowPlayingFragment).commit()

        initHeader album
    }

    @Override
    protected void initEventHandlersOnCreate() {
        super.initEventHandlersOnCreate()
        RxBus.on(CurrentSongAvailableEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(PlaybackStartedEvent).bindToLifecycle(this).subscribe(this.&onEvent)
    }

    private void subscribeToScrollEvents() {
        recycler.addOnScrollListener recyclerScrollListener

        int scrollableHeight = 0

        int toolbarHeightsDifference = 0
        int originalToolbarHeight = 0

        appBarLayout.viewTreeObserver.addOnGlobalLayoutListener {
            scrollableHeight = resources.displayMetrics.widthPixels - toolbar.minimumHeight
            originalToolbarHeight = toolbar.height
            toolbarHeightsDifference = toolbar.height - toolbar.minimumHeight
        }

        scrollSubscribers = new CompositeSubscription()

        int statusbarHeight = Utils.dpToPx(24)

        def scrolledPercent = RxAppBarLayout.offsetChanges(appBarLayout)
                .doOnNext { Debug.d it as String }
                .map { (1 - (scrollableHeight - statusbarHeight + it) / (float) scrollableHeight) as float }

        def desiredToolbarAlpha = scrolledPercent.map { (1 - it) * 180 as int }

        scrollSubscribers.add desiredToolbarAlpha
                .subscribe {
            toolbar.backgroundColor = Color.argb(it, 0, 0, 0)
        }

        def bufferedScrollPercent = scrolledPercent.buffer(2)

        scrollSubscribers.add bufferedScrollPercent
                .skipWhile { it[1] < it[0] }
                .first()
                .repeat()
                .subscribe {
            fab.hide()
        }

        scrollSubscribers.add bufferedScrollPercent
                .skipWhile { it[1] > it[0] }
                .first()
                .repeat()
                .subscribe {
            fab.show()
        }
    }

    private void unsubscribeFromScrollEvents() {
        recycler.removeOnScrollListener recyclerScrollListener
        scrollSubscribers.unsubscribe()
    }

    private void initHeader(Album album) {
        Glide.with(this)
                .load("file://$album.albumArtPath" as String)
                .centerCrop()
                .error(R.drawable.no_cover)
                .placeholder(R.color.grid_item_default_bg)
                .into(image)

        title.text = album.title
        subtitle.text = album.year ? "$album.artistName • $album.year" : album.artistName as String
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.album_info, menu);
//        return true;
//    }

    @Override
    void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_to_queue:
                RxBus.post new EnqueueCommand(songs)
                Toast toast = Toast.makeText(this, R.string.album_added_to_queue, Toast.LENGTH_SHORT)
                toast.show()
                return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Override
    protected void onDestroy() {
        unsubscribeFromScrollEvents()
        super.onDestroy()
    }

    // region Event handlers

    private void onEvent(CurrentSongAvailableEvent event) {
        if (event.song)
            onEvent new PlaybackStartedEvent()
    }

    private void onEvent(PlaybackStartedEvent event) {
        nowPlayingFragment.show(supportFragmentManager).subscribe { int height ->
            def a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    RelativeLayout.LayoutParams params = coordinator.layoutParams as RelativeLayout.LayoutParams
                    params.bottomMargin = height * interpolatedTime as int
                    coordinator.layoutParams = params
                }
            }

            a.duration = 150
            coordinator.startAnimation a
        }
//        highlightCurrentSong event.song
    }

    // endregion
}