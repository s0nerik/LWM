package app.ui.fragment.playback
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.Nullable
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import app.R
import app.Utils
import app.events.access_point.AccessPointStateEvent
import app.events.player.RepeatStateChangedEvent
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongChangedEvent
import app.events.player.playback.SongPlayingEvent
import app.events.player.queue.QueueShuffledEvent
import app.helper.wifi.WifiAP
import app.model.Song
import app.player.BasePlayer
import app.player.LocalPlayer
import app.player.PlayerUtils
import com.bumptech.glide.Glide
import com.github.s0nerik.betterknife.annotations.OnClick
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

import static app.events.access_point.AccessPointStateEvent.State.ENABLED

@CompileStatic
class LocalPlaybackFragment extends PlaybackFragment {

    @Inject
    @PackageScope
    LocalPlayer player

    @Inject
    @PackageScope
    WifiAP wifiAP

    @Inject
    @PackageScope
    WindowManager windowManager

    private View chatButton

    @Override
    void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)
        seekBar.onSeekBarChangeListener = new PlayerProgressOnSeekBarChangeListener()
        initToolbar()
    }

    private void initToolbar() {
        toolbar.inflateMenu R.menu.playback_local
        chatButton = toolbar.findViewById(R.id.action_chat)
        chatButtonVisibility = wifiAP.enabled
    }

    private void setChatButtonVisibility(boolean show) {
        chatButton.visibility = show ? View.VISIBLE : View.GONE
    }

    @OnClick([R.id.btnPlayPause, R.id.btnNext, R.id.btnPrev, R.id.btnShuffle, R.id.btnRepeat])
    void onClickControls(View btn) {
        switch (btn.getId()) {
            case R.id.btnNext:
                player.nextSong()
                break;
            case R.id.btnPrev:
                player.prevSong()
                break;
            case R.id.btnPlayPause:
                player.togglePause()
                break;
            case R.id.btnShuffle:
                player.shuffleQueueExceptPlayed()
                break;
            case R.id.btnRepeat:
                player.repeat = !player.repeat
                break;
        }
    }

    @Override
    protected BasePlayer getPlayer() { player }

    @Override
    protected void setSongInfo(Song song) {
        super.setSongInfo(song)

        // Load cover into cover view
        final Drawable prevDrawable = Utils.copyDrawable(cover.drawable)
        Glide.with(this)
                .load(song.getAlbumArtUri().toString())
                .centerCrop()
                .placeholder(prevDrawable)
                .error(R.drawable.no_cover)
                .crossFade()
                .into(cover)

        // Load blurred cover into background view
        final Drawable prevBgDrawable = Utils.copyDrawable(background.drawable)
        Glide.with(this)
                .load(song.getAlbumArtUri().toString())
                .placeholder(prevBgDrawable)
                .centerCrop()
//                .transform(new BitmapTransformation(activity) {
//                    @Override
//                    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
//                        return new Blur().blur(toTransform);
//                    }
//
//                    @Override
//                    String getId() {
//                        return "blur_bg_" + song.getTitle()
//                    }
//                })
                .crossFade()
                .into(background)
    }

    @Subscribe
    void onAccessPointStateChanged(AccessPointStateEvent event) {
        chatButtonVisibility = event.state == ENABLED
    }

    @Subscribe
    @Override
    void onSongChanged(SongChangedEvent event) {
        super.onSongChanged(event);
    }

    @Subscribe
    @Override
    void onPlaybackStarted(PlaybackStartedEvent event) {
        super.onPlaybackStarted(event);
    }

    @Subscribe
    @Override
    void onPlaybackPaused(PlaybackPausedEvent event) {
        super.onPlaybackPaused(event);
    }

    @Subscribe
    @Override
    void onSongPlaying(SongPlayingEvent event) {
        super.onSongPlaying(event);
    }

    @Subscribe
    @Override
    void onQueueShuffled(QueueShuffledEvent event) {
        super.onQueueShuffled(event);
    }

    @Subscribe
    @Override
    void onRepeatStateChanged(RepeatStateChangedEvent event) {
        super.onRepeatStateChanged(event);
    }

    class PlayerProgressOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        PlayerProgressOnSeekBarChangeListener() {}

        @Override
        void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                player.seekTo PlayerUtils.convertSeekBarToProgress(progress)
            }
        }

        @Override
        void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
