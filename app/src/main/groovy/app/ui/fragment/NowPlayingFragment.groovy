package app.ui.fragment

import android.animation.ValueAnimator
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import app.R
import app.Utils
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongChangedEvent
import app.model.Song
import app.player.LocalPlayer
import app.ui.BlurTransformation
import app.ui.activity.LocalPlaybackActivity
import app.ui.base.DaggerFragment
import app.ui.custom_view.RadialEqualizerView
import com.bumptech.glide.Glide
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnClick
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.*
import rx.*
import rx.android.schedulers.AndroidSchedulers

import javax.inject.Inject
import java.util.concurrent.TimeUnit

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
@InjectLayout(value = R.layout.fragment_now_playing, injectAllViews = true)
class NowPlayingFragment extends DaggerFragment {

    @Inject
    Utils utils
    @Inject
    Bus bus
    @Inject
    LocalPlayer player

    ImageView cover
    TextView title
    TextView artist
    View fabGroup
    View mainGroup
    View layout
    RadialEqualizerView radialEqualizerView

    private Subscription radialEqualizerViewSubscription

    @Override
    void onResume() {
        super.onResume()
        bus.register this
        Song song = player.currentSong
        if (song != null) {
            songInfo = player.currentSong
        }
    }

    @Override
    void onPause() {
        bus.unregister this
        super.onPause()
    }

    Observable<Integer> show() {
        Observable.create({ Subscriber<Integer> subscriber ->
            mainGroup.translationY = mainGroup.height
            fabGroup.scaleX = 0
            fabGroup.scaleY = 0

            mainGroup.animate()
                    .translationY(0)
                    .setDuration(1000)
                    .withEndAction({
                        subscriber.onNext(mainGroup.height - (mainGroup.layoutParams as RelativeLayout.LayoutParams).topMargin)
                        subscriber.onCompleted()

                        fabGroup.animate()
                                .scaleX(1)
                                .scaleY(1)
                                .setDuration(1000)
                                .start()
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

    @Subscribe
    void onSongChanged(SongChangedEvent event) {
        songInfo = event.song
    }

    @Subscribe
    void onSongPlaybackStarted(PlaybackStartedEvent e) {
        radialEqualizerViewSubscription?.unsubscribe()

        radialEqualizerViewSubscription = Observable.interval(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
            radialEqualizerView.randomize()
        }
    }

}
