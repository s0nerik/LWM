package app.ui.fragment.playback
import android.content.res.Resources
import android.net.Uri
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

    protected abstract BasePlayer getPlayer()

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
        seekBar.progress = PlayerUtils.calculateProgressForSeekBar event.progress
        currentTime.text = player.currentPositionInMinutes
    }

    protected void onSongChanged(SongChangedEvent event) {
        setSongInfo event.song
    }

    protected void onPlaybackStarted(PlaybackStartedEvent event) {
        Debug.d "onPlaybackStarted"
        setPlayButton player.playing
    }

    protected void onPlaybackPaused(PlaybackPausedEvent event) {
        Debug.d "onPlaybackPaused"
        setPlayButton player.playing
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
        seekBar.progress = PlayerUtils.calculateProgressForSeekBar player.currentPosition
        setPlayButton player.playing
        setShuffleButton player.shuffle
        setRepeatButton player.repeat
    }

}
