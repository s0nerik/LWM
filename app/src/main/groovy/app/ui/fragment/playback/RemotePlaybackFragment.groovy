package app.ui.fragment.playback
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import app.R
import app.Utils
import app.events.player.playback.SongChangedEvent
import app.events.player.playback.SongPlayingEvent
import app.model.Song
import app.player.BasePlayer
import app.player.StreamPlayer
import app.ui.Blurer
import com.koushikdutta.ion.Ion
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import rx.Observable
import rx.Subscriber

import javax.inject.Inject

@CompileStatic
class RemotePlaybackFragment extends PlaybackFragment {

    @Inject
    @PackageScope
    StreamPlayer player

    // TODO: remove this when error in Groovy is fixed
    @Inject
    @PackageScope
    Blurer blurer

    @Inject
    @PackageScope
    Utils utils

    @Override
    void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)
        controls.visibility = View.GONE
        seekBar.enabled = false
        seekBar.backgroundResource = R.drawable.background_seekbar_no_controls
    }

    @Override
    protected BasePlayer getPlayer() { player }

    @Override
    protected Observable<Bitmap> getCoverBitmap(Song song) {
        Observable.create({ Subscriber<Bitmap> subscriber ->
            Bitmap bmp = null
            try {
                bmp = Ion.with(this)
                            .load("${player.currentSong.albumArtUri}?${UUID.randomUUID()}")
                            .asBitmap()
                            .get()
            } catch (ignored) {}

            bmp = bmp ?: utils.noCoverBitmap

            subscriber.onNext bmp
            subscriber.onCompleted()
        } as Observable.OnSubscribe<Bitmap>)
    }

    @Subscribe
    @Override
    void onSongChanged(SongChangedEvent event) {
        super.onSongChanged(event);
    }

    @Subscribe
    @Override
    void onSongPlaying(SongPlayingEvent event) {
        super.onSongPlaying(event)
    }
}
