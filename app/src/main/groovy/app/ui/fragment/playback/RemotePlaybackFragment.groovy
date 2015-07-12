package app.ui.fragment.playback

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import app.R
import app.events.player.RepeatStateChangedEvent
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongChangedEvent
import app.events.player.playback.SongPlayingEvent
import app.events.player.queue.QueueShuffledEvent
import app.model.Song
import app.player.BasePlayer
import app.player.StreamPlayer
import app.server.StreamServer
import app.ui.Blur
import com.koushikdutta.ion.Ion
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug
import rx.Observable

import javax.inject.Inject

@CompileStatic
class RemotePlaybackFragment extends PlaybackFragment {

    @Inject
    @PackageScope
    StreamPlayer player

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
        Observable.just(
            Ion.with(this)
                    .load("${StreamServer.Url.CURRENT_ALBUMART}?${UUID.randomUUID()}")
                    .asBitmap()
                    .get()
        )
    }

    @Override
    protected Observable<Bitmap> getBgBitmap(Song song) {
        Observable.just(
                new Blur().blur(
                    Ion.with(this)
                            .load("${StreamServer.Url.CURRENT_ALBUMART}?${UUID.randomUUID()}")
                            .asBitmap()
                            .get()
                )
        )
    }

    @Subscribe
    @Override
    void onSongChanged(SongChangedEvent event) {
        Debug.d "RemotePlaybackFragment: onSongChanged"
    }

    @Subscribe
    @Override
    void onPlaybackStarted(PlaybackStartedEvent event) {
        songInfo = event.song
        Debug.d "RemotePlaybackFragment: onPlaybackStarted"
    }

    @Subscribe
    @Override
    void onPlaybackPaused(PlaybackPausedEvent event) {
        Debug.d "RemotePlaybackFragment: onPlaybackPaused"
    }

    @Subscribe
    @Override
    void onSongPlaying(SongPlayingEvent event) {
        super.onSongPlaying(event)
    }

    @Subscribe
    @Override
    void onQueueShuffled(QueueShuffledEvent event) {
        Debug.d "RemotePlaybackFragment: onQueueShuffled"
    }

    @Subscribe
    @Override
    void onRepeatStateChanged(RepeatStateChangedEvent event) {
        Debug.d "RemotePlaybackFragment: onRepeatStateChanged"
    }

}
