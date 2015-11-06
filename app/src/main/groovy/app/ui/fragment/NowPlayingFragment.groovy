package app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.FragmentManager
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import app.R
import app.Utils
import app.commands.ChangePauseStateCommand
import app.events.player.playback.*
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
    FloatingActionButton playbackFab

    private Subscription radialEqualizerViewSubscription

    @Override
    void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)

        view.visibility = View.GONE
    }

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

    Observable<Integer> show(FragmentManager fragmentManager) {
        Observable.create({ Subscriber<Integer> subscriber ->
            fragmentManager.beginTransaction().show(this).commitAllowingStateLoss()
            view.visibility = View.VISIBLE

            mainGroup.translationY = mainGroup.height
            fabGroup.scaleX = 0
            fabGroup.scaleY = 0

            mainGroup.animate()
                    .translationY(0)
                    .setDuration(500)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction({
                        subscriber.onNext(mainGroup.height - mainGroup.paddingTop)
                        subscriber.onCompleted()

                        fabGroup.animate()
                                .scaleX(1)
                                .scaleY(1)
                                .setDuration(250)
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

    @OnClick(R.id.playbackFab)
    void onFabClicked() {
        player.togglePause()
    }

    @Subscribe
    void onSongChanged(SongChangedEvent event) {
        songInfo = event.song
    }

    @Subscribe
    void onSongPlaybackStarted(PlaybackStartedEvent e) {
        (playbackFab as ImageView).imageResource = R.drawable.ic_pause_24dp

        radialEqualizerViewSubscription?.unsubscribe()

        radialEqualizerViewSubscription = Observable.interval(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
            radialEqualizerView.randomize()
        }
    }

    @Subscribe
    void onSongPlaybackStarted(PlaybackPausedEvent e) {
        (playbackFab as ImageView).imageResource = R.drawable.ic_play_arrow_24dp

        radialEqualizerViewSubscription?.unsubscribe()
    }
}
