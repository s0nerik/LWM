package app.ui.activity
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.ImageView
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

    CollapsingToolbarLayout collapsingToolbar
    Toolbar toolbar
    ImageView image
    RecyclerView recycler

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
        bus.register this

        songs = album.songs.toList().toBlocking().first()

        def adapter = new SongsListAdapter(this, songs)
        def layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.hasFixedSize = true

        initHeader album
    }

    private void initHeader(Album album) {
        Glide.with(this)
                .load("file://$album.albumArtPath")
                .centerCrop()
                .error(R.drawable.no_cover)
                .placeholder(R.color.grid_item_default_bg)
                .into(image)

        collapsingToolbar.setTitle album.title
        toolbar.setSubtitle utils.getArtistName(album.artistName)
    }

//    @OnClick(R.id.fab)
//    public void onFabClicked() {
//        bus.post new SetQueueAndPlayCommand(songs, 0)
//    }
//
//    @OnClick(R.id.overflowMenu)
//    public void onOverflowMenuClicked(View view) {
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_info, menu);
        return true;
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
//    }

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
    void playbackStarted(PlaybackStartedEvent event) {
//        highlightCurrentSong event.song
    }

    @Subscribe
    void currentSongAvailable(CurrentSongAvailableEvent event) {
//        highlightCurrentSong event.song
    }
}