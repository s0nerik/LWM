package app.ui.fragment
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import app.R
import app.Utils
import app.events.player.playback.SongChangedEvent
import app.model.Song
import app.player.LocalPlayer
import app.ui.BlurTransformation
import app.ui.activity.LocalPlaybackActivity
import app.ui.base.DaggerFragment
import com.bumptech.glide.Glide
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnClick
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
                .bitmapTransform(new BlurTransformation(activity, Glide.get(activity).getBitmapPool()))
                .placeholder(android.R.color.black)
                .error(R.drawable.no_cover_blurred)
                .crossFade()
                .into(cover)

        artist.text = utils.getArtistName song.artistName
        title.text = song.title
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
