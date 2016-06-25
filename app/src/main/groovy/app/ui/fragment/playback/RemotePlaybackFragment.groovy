package app.ui.fragment.playback

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import app.App
import app.R
import app.Utils
import app.events.player.playback.SongChangedEvent
import app.events.player.playback.SongPlayingEvent
import app.models.Song
import app.players.BasePlayer
import app.players.StreamPlayer
import app.ui.Blurer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import rx.Observable
import rx.Subscriber

import javax.inject.Inject

@CompileStatic
class RemotePlaybackFragment extends PlaybackFragment {

    @Inject
    protected StreamPlayer player

    // TODO: remove this when error in Groovy is fixed
    @Inject
    protected Blurer blurer

    @Inject
    protected Utils utils

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        App.get().inject(this)
    }

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
                bmp = Glide.with(this)
                        .load("${player.currentSong.albumArtUri}?${player.currentSong.id}")
                        .asBitmap()
                        .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
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
