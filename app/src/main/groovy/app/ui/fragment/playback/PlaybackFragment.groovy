package app.ui.fragment.playback
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.*
import app.R
import app.Utils
import app.events.player.RepeatStateChangedEvent
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongChangedEvent
import app.events.player.playback.SongPlayingEvent
import app.events.player.queue.QueueShuffledEvent
import app.model.Song
import app.player.BasePlayer
import app.player.PlayerUtils
import app.ui.async.RemoteAlbumArtAsyncGetter
import app.ui.base.DaggerOttoOnResumeFragment
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.nvanbenschoten.motion.ParallaxImageView
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug
import rx.Observable

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = R.layout.fragment_playback, injectAllViews = true)
abstract class PlaybackFragment extends DaggerOttoOnResumeFragment {

    @Inject
    @PackageScope
    Resources res

    ParallaxImageView background
    ImageView cover
    FrameLayout albumArtLayout
    TextView currentTime
    TextView endTime
    SeekBar seekBar
    ImageView btnShuffleIcon
    ImageView btnPrevIcon
    ImageView btnPlayPauseIcon
    ImageView btnNextIcon
    ImageView btnRepeatIcon
    LinearLayout controls
    LinearLayout bottomBar
    ProgressBar progressBar
    Toolbar toolbar

    TransitionDrawable bgDrawable
    TransitionDrawable coverDrawable

    protected abstract BasePlayer getPlayer()
    protected abstract Observable<Bitmap> getCoverBitmap(Song song)
    protected abstract Observable<Bitmap> getBgBitmap(Song song)

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        if (!savedInstanceState) {
            def blackDrawables = [new ColorDrawable(Color.BLACK)] * 2 as Drawable[]

            coverDrawable = new TransitionDrawable(blackDrawables)
            coverDrawable.crossFadeEnabled = true

            bgDrawable = new TransitionDrawable(blackDrawables)
            bgDrawable.crossFadeEnabled = true
        }
    }

    @Override
    void onResume() {
        super.onResume()
        initView()
        background.registerSensorManager()
    }

    @Override
    void onPause() {
        super.onPause()
        background.unregisterSensorManager()
    }

    protected void onSongPlaying(SongPlayingEvent event) {
        seekBar.progress = PlayerUtils.calculateProgressForSeekBar event.progress as int
        currentTime.text = player.currentPositionInMinutes
    }

    protected void onSongChanged(SongChangedEvent event) {
        setSongInfo event.song
    }

    protected void onPlaybackStarted(PlaybackStartedEvent event) {
        Debug.d()
        setPlayButton true
    }

    protected void onPlaybackPaused(PlaybackPausedEvent event) {
        Debug.d()
        setPlayButton false
    }

    protected void onQueueShuffled(QueueShuffledEvent event) {
        setShuffleButton player.shuffle
    }

    protected void onRepeatStateChanged(RepeatStateChangedEvent event) {
        setRepeatButton player.repeat
    }

    protected void setSongInfo(final Song song) {
        if (!song) return
        toolbar.setTitle song.title
        toolbar.setSubtitle song.artist

        seekBar.max = PlayerUtils.calculateProgressForSeekBar song.duration
        endTime.text = song.durationString

        getCoverBitmap(song).subscribe { Bitmap b ->
            coverDrawable = new TransitionDrawable([coverDrawable.getDrawable(1), new BitmapDrawable(b)] as Drawable[])
            cover.imageDrawable = coverDrawable
            coverDrawable.startTransition 1000
        }

        getBgBitmap(song).subscribe { Bitmap b ->
            bgDrawable = new TransitionDrawable([bgDrawable.getDrawable(1), new BitmapDrawable(b)] as Drawable[])
            background.imageDrawable = bgDrawable
            bgDrawable.startTransition 1000
        }
    }

    protected void setAlbumArtFromUri(Uri uri) {
        Utils.setAlbumArtFromUri activity, cover, uri
    }

    public void setRemoteAlbumArt() {
        RemoteAlbumArtAsyncGetter remoteAlbumArtAsyncGetter = new RemoteAlbumArtAsyncGetter(activity, cover, background)
        remoteAlbumArtAsyncGetter.execute()
    }

    private void setPlayButton(boolean playing) {
        if (playing) {
            btnPlayPauseIcon.imageResource = R.drawable.ic_av_pause
        } else {
            btnPlayPauseIcon.imageResource = R.drawable.ic_av_play_arrow
        }
    }

    private void setShuffleButton(boolean enabled) {
        btnShuffleIcon.colorFilter = res.getColor enabled? R.color.primary : android.R.color.white
    }

    private void setRepeatButton(boolean enabled) {
        btnRepeatIcon.colorFilter = res.getColor enabled? R.color.primary : android.R.color.white
    }

    private void initView() {
        setSongInfo player.currentSong
        currentTime.text = player.currentPositionInMinutes
        seekBar.progress = PlayerUtils.calculateProgressForSeekBar player.currentPosition as int
        setPlayButton player.playing
        setShuffleButton player.shuffle
        setRepeatButton player.repeat
    }

}
