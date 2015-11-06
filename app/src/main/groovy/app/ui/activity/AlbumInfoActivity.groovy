package app.ui.activity

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
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
import app.R.layout
import app.Utils
import app.adapter.SongsListAdapter
import app.commands.EnqueueCommand
import app.data_managers.SongsManager
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.model.Album
import app.model.Song
import app.ui.fragment.NowPlayingFragment
import com.bumptech.glide.Glide
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.Extra
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = layout.activity_album_info, injectAllViews = true)
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

    @Inject
    @PackageScope
    LayoutInflater inflater

    @Inject
    @PackageScope
    Utils utils

    @Inject
    @PackageScope
    SongsManager songsManager

    @Extra
    Album album

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState)
        BetterKnife.loadExtras this

        songs = album.songs.toList().toBlocking().first()

        def adapter = new SongsListAdapter(this, songs)
        def layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.hasFixedSize = true

//        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//            @Override
//            void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//
//            }
//        })

        recycler.addOnScrollListener new RecyclerView.OnScrollListener() {
            private boolean collapsed
            private boolean expanded

            @Override
            void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && !collapsed) {
                    appBarLayout.setExpanded(false, true)
                    collapsed = true
                    expanded = false

                    toolbar.setNavigationIcon R.drawable.ic_arrow_back_white_24dp
                } else if (dy < 0 && !expanded && layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    appBarLayout.setExpanded(true, true)
                    collapsed = false
                    expanded = true

                    toolbar.setNavigationIcon null
                }
            }
        }

        nowPlayingFragment = supportFragmentManager.findFragmentById(R.id.nowPlayingFragment) as NowPlayingFragment
        supportFragmentManager.beginTransaction().hide(nowPlayingFragment).commit()

        initHeader album

        bus.register this
    }

    private void initHeader(Album album) {
        Glide.with(this)
                .load("file://$album.albumArtPath" as String)
                .centerCrop()
                .error(R.drawable.no_cover)
                .placeholder(R.color.grid_item_default_bg)
                .into(image)

        title.text = album.title
        subtitle.text = "$album.artistName • $album.year" as String
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
                bus.post new EnqueueCommand(songs)
                Toast toast = Toast.makeText(this, R.string.album_added_to_queue, Toast.LENGTH_SHORT)
                toast.show()
                return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }

    @Subscribe
    void onSongAvailable(CurrentSongAvailableEvent event) {
        if (event.song)
            playbackStarted null
    }

    @Subscribe
    void playbackStarted(PlaybackStartedEvent event) {
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

    @Subscribe
    void currentSongAvailable(CurrentSongAvailableEvent event) {
//        highlightCurrentSong event.song
    }
}