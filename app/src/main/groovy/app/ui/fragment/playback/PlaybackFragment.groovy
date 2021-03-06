package app.ui.fragment.playback

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
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
import app.models.Song
import app.players.BasePlayer
import app.players.PlayerUtils
import com.github.s0nerik.rxbus.RxBus
import app.ui.Blurer
import app.ui.base.BaseFragment
import com.github.s0nerik.betterknife.annotations.InjectLayout
import groovy.transform.CompileStatic
import ru.noties.debug.Debug
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func1

import javax.inject.Inject
import java.util.concurrent.TimeUnit

@CompileStatic
@InjectLayout(value = R.layout.fragment_playback, injectAllViews = true)
abstract class PlaybackFragment extends BaseFragment {

    @Inject
    protected Resources res

    @Inject
    protected Blurer blurer

    @Inject
    protected Utils utils

    ImageView background
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
    }

    @Override
    protected void initEventHandlersOnResume() {
        super.initEventHandlersOnResume()
        RxBus.on(SongPlayingEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(SongChangedEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(PlaybackStartedEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(PlaybackPausedEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(QueueShuffledEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(RepeatStateChangedEvent).bindToLifecycle(this).subscribe(this.&onEvent)
    }

    protected void setSongInfo(final Song song) {
        if (!song) return
        toolbar.setTitle song.title
        toolbar.setSubtitle utils.getArtistName(song.artistName)

        seekBar.max = PlayerUtils.calculateProgressForSeekBar song.duration
        endTime.text = song.durationString

        getCoverBitmap(song)
                .concatMap({ Bitmap original -> blurer.blurAsObservable(original).map { new Tuple2(original, it) } } as Func1)
                .applySchedulers()
                .subscribe { Tuple2<Bitmap, Bitmap> covers -> changeCover covers.first, covers.second }
    }

    private void changeCover(Bitmap newCover, Bitmap bg) {
        coverDrawable = new TransitionDrawable([coverDrawable.getDrawable(1), new BitmapDrawable(newCover)] as Drawable[])
        cover.imageDrawable = coverDrawable
        coverDrawable.startTransition 1000

        Observable.timer(500, TimeUnit.MILLISECONDS)
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe {
            bgDrawable = new TransitionDrawable([bgDrawable.getDrawable(1), new BitmapDrawable(bg)] as Drawable[])
            background.imageDrawable = bgDrawable
            bgDrawable.startTransition 1000
        }
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

    // region Event handlers

    protected void onEvent(SongPlayingEvent event) {
        seekBar.progress = PlayerUtils.calculateProgressForSeekBar event.progress as int
        currentTime.text = player.currentPositionInMinutes
    }

    protected void onEvent(SongChangedEvent event) {
        setSongInfo event.song
    }

    protected void onEvent(PlaybackStartedEvent event) {
        Debug.d()
        setPlayButton true
    }

    protected void onEvent(PlaybackPausedEvent event) {
        Debug.d()
        setPlayButton false
    }

    protected void onEvent(QueueShuffledEvent event) {
        setShuffleButton player.shuffle
    }

    protected void onEvent(RepeatStateChangedEvent event) {
        setRepeatButton player.repeat
    }

    // endregion

}
