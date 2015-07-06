package app.ui.fragment
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.R
import app.Utils
import app.events.player.playback.SongChangedEvent
import app.model.Song
import app.player.LocalPlayer
import app.ui.activity.LocalPlaybackActivity
import app.ui.base.DaggerFragment
import com.bumptech.glide.Glide
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnClick
import com.nineoldandroids.view.ViewHelper
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
@InjectLayout(value = R.layout.fragment_now_playing, injectAllViews = true)
public class NowPlayingFragment extends DaggerFragment {

    @Inject
    Utils utils
    @Inject
    Bus bus
    @Inject
    LocalPlayer player

    ImageView cover
    TextView title
    TextView artist
    View nowPlayingLayout
    View shadow

    @Override
    public void onResume() {
        super.onResume()
        bus.register this
        Song song = player.currentSong
        if (song != null) {
            songInfo = player.currentSong
        }
    }

    @Override
    public void onPause() {
        bus.unregister this
        super.onPause()
    }

    public void setSongInfo(Song song) {
        Glide.with(cover.context)
                .load(song.getAlbumArtUri().toString())
                .centerCrop()
                .placeholder(R.drawable.no_cover)
                .error(R.drawable.no_cover)
//                .withBitmapInfo()
//                .setCallback(new SingleBitmapPaletteInfoCallback(nowPlayingLayout, shadow, title, artist));

        // TODO: generate palette

        artist.setText(utils.getArtistName(song.getArtist()));
        title.setText(song.getTitle());

        ViewHelper.setAlpha(shadow, 0.9f);
        ViewHelper.setAlpha(nowPlayingLayout, 0.9f);
    }

    @OnClick([R.id.layout, R.id.cover])
    public void onLayoutClicked() {
        Intent intent = new Intent(activity, LocalPlaybackActivity)
        startActivity intent
        activity.overridePendingTransition R.anim.slide_in_right, R.anim.slide_out_left_long_alpha
    }

    @Subscribe
    public void onSongChanged(SongChangedEvent event) {
        songInfo = event.song
    }

}
