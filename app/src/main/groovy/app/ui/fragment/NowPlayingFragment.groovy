package app.ui.fragment

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.FragmentManager
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import app.App
import app.R
import app.Utils
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongChangedEvent
import app.events.player.playback.SongPlayingEvent
import app.models.Song
import app.players.LocalPlayer
import app.ui.activity.LocalPlaybackActivity
import app.ui.base.BaseFragment
import app.ui.custom_view.RadialEqualizerView
import com.bumptech.glide.Glide
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem
import com.github.rahatarmanahmed.cpv.CircularProgressView
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnClick
import com.github.s0nerik.rxbus.RxBus
import groovy.transform.CompileStatic
import jp.wasabeef.glide.transformations.BlurTransformation
import rx.Observable
import rx.Subscriber
import rx.Subscription

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = R.layout.fragment_now_playing, injectAllViews = true)
class NowPlayingFragment extends BaseFragment {

    @Inject
    protected Utils utils
    @Inject
    protected LocalPlayer player

    ImageView cover
    TextView title
    TextView artist
    View btnPlayPause
    View mainGroup
    View layout
    RadialEqualizerView radialEqualizerView
    FloatingActionButton playbackFab

    ProgressBar mainProgress
    CircularProgressView circleProgress
    View circleProgressBg
    View circleProgressShadow

    private Subscription radialEqualizerViewSubscription

    @Override
    void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)

        view.visibility = View.GONE
    }

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        App.get().inject this
        initEventHandlers()
    }

    private void initEventHandlers() {
        RxBus.on(SongChangedEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(PlaybackStartedEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(PlaybackPausedEvent).bindToLifecycle(this).subscribe(this.&onEvent)
        RxBus.on(SongPlayingEvent).bindToLifecycle(this).subscribe(this.&onEvent)
    }

    Observable<Integer> show(FragmentManager fragmentManager) {
        Observable.create({ Subscriber<Integer> subscriber ->
            fragmentManager.beginTransaction().show(this).commitAllowingStateLoss()
            view.visibility = View.VISIBLE

            mainGroup.translationY = mainGroup.height
            btnPlayPause.scaleX = 0
            btnPlayPause.scaleY = 0

            circleProgressBg.scaleX = 0
            circleProgressBg.scaleY = 0

            circleProgressShadow.scaleX = 0
            circleProgressShadow.scaleY = 0

            mainGroup.animate()
                    .translationY(0)
                    .setDuration(500)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction({
                        subscriber.onNext(mainGroup.height - mainGroup.paddingTop)
                        subscriber.onCompleted()

                        SpringSystem springSystem = SpringSystem.create()
                        Spring spring = springSystem.createSpring()
                        spring.addListener(new SimpleSpringListener() {
                            @Override
                            public void onSpringUpdate(Spring s) {
                                float value = (float) s.currentValue
//                                float scale = 1f - (value * 0.5f)
                                float scale = value

                                btnPlayPause.scaleX = scale
                                btnPlayPause.scaleY = scale

                                circleProgressBg.scaleX = scale
                                circleProgressBg.scaleY = scale

                                circleProgressShadow.scaleX = scale
                                circleProgressShadow.scaleY = scale
                            }
                        })
                        spring.endValue = 1
                    })
                    .start()
        } as Observable.OnSubscribe<Integer>)
    }

    void setSongInfo(Song song) {
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
    void onLayoutClicked() {
        Intent intent = new Intent(activity, LocalPlaybackActivity)
        startActivity intent
        activity.overridePendingTransition R.anim.slide_in_right, R.anim.slide_out_left_long_alpha
    }

    @OnClick(R.id.playbackFab)
    void onFabClicked() {
        player.togglePause()
    }

    // region Event handlers

    private void onEvent(SongChangedEvent event) {
        songInfo = event.song
    }

    private void onEvent(PlaybackStartedEvent e) {
        (playbackFab as ImageView).imageResource = R.drawable.ic_pause_24dp

//        radialEqualizerViewSubscription?.unsubscribe()
//
//        radialEqualizerViewSubscription = Observable.interval(200, TimeUnit.MILLISECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe {
//            radialEqualizerView.randomize()
//        }
    }

    private void onEvent(PlaybackPausedEvent e) {
        (playbackFab as ImageView).imageResource = R.drawable.ic_play_arrow_24dp

        radialEqualizerViewSubscription?.unsubscribe()
    }

    private void onEvent(SongPlayingEvent e) {
        progress = e.progressPercent
    }

    // endregion

    private void setProgress(float progress) {
        Rect circleRect = new Rect()
        playbackFab.getGlobalVisibleRect(circleRect)
        float circleStartPercent = 100f - (mainProgress.width - circleRect.left) / (float) mainProgress.width * 100f as float
        float circleEndPercent = 100f - (mainProgress.width - circleRect.right) / (float) mainProgress.width * 100f as float
        if (progress > circleStartPercent && progress < circleEndPercent) {
            float circlePercents = circleEndPercent - circleStartPercent
            circleProgress.progress = ((progress - circleStartPercent) / circlePercents) * 100f as float
            mainProgress.progress = ((circleEndPercent + circleStartPercent) / 2f) * 10f as int
        } else if (progress <= circleStartPercent) {
            mainProgress.progress = progress * 10f as int
            circleProgress.progress = 0f
        } else if (progress >= circleEndPercent) {
            mainProgress.progress = progress * 10f as int
            circleProgress.progress = 100f
        }
    }
}
