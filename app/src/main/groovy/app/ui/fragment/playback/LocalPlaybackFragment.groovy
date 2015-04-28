package app.ui.fragment.playback

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.Nullable
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.SeekBar
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
import app.ui.Blur
import com.arasthel.swissknife.annotations.OnClick
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.koushikdutta.ion.bitmap.Transform
import app.R
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic

import javax.inject.Inject

import static app.events.access_point.AccessPointStateEvent.State.ENABLED

@CompileStatic
public class LocalPlaybackFragment extends PlaybackFragment {

    @Inject
    LocalPlayer player;

    @Inject
    WifiAP wifiAP;

    @Inject
    WindowManager windowManager;

    private View chatButton;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSeekBar.onSeekBarChangeListener = new PlayerProgressOnSeekBarChangeListener()
        initToolbar();
    }

    private void initToolbar() {
        mToolbar.inflateMenu(R.menu.playback_local);
        chatButton = mToolbar.findViewById(R.id.action_chat);
        setChatButtonVisibility(wifiAP.isEnabled());
    }

    private void setChatButtonVisibility(boolean show) {
        chatButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @OnClick([R.id.btnPlayPause, R.id.btnNext, R.id.btnPrev, R.id.btnShuffle, R.id.btnRepeat])
    public void onClickControls(View btn) {
        switch (btn.getId()) {
            case R.id.btnNext:
                player.nextSong();
                break;
            case R.id.btnPrev:
                player.prevSong();
                break;
            case R.id.btnPlayPause:
                player.togglePause();
                break;
            case R.id.btnShuffle:
                player.shuffleQueueExceptPlayed();
                break;
            case R.id.btnRepeat:
                player.setRepeat(!player.isRepeat());
                break;
        }
    }

    @Override
    protected BasePlayer getPlayer() {
        return player;
    }

    @Override
    protected void setCover(Song song) {
//        mAlbumArtLayout.setAlpha(0f);
        final Drawable prevDrawable = mCover.getDrawable().getConstantState().newDrawable();
        Ion.with(mCover)
                .crossfade(false)
                .placeholder(prevDrawable)
                .error(R.drawable.no_cover)
                .crossfade(true)
                .smartSize(true)
//                .fadeIn(false)
                .load(song.getAlbumArtUri().toString())
                .setCallback(new FutureCallback<ImageView>() {
                    @Override
                    public void onCompleted(Exception e, ImageView result) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
//                                mAlbumArtLayout.setAlpha(1f);
//                                YoYo.with(Techniques.BounceIn)
//                                        .duration(500)
//                                        .playOn(mAlbumArtLayout);
                            }
                        });
                    }
                });
    }

    @Override
    protected void setBackground(final Song song) {
        final Drawable prevDrawable = mBackground.getDrawable().getConstantState().newDrawable();
        Ion.with(mBackground)
                .placeholder(prevDrawable)
                .crossfade(true)
                .smartSize(true)
                .transform(new Transform() {
                    @Override
                    public Bitmap transform(Bitmap b) {
                        return new Blur().blur(b);
                    }

                    @Override
                    public String key() {
                        return "blur_bg_" + song.getTitle();
                    }
                })
                .load(song.getAlbumArtUri().toString())
                .setCallback(new FutureCallback<ImageView>() {
                    @Override
                    public void onCompleted(Exception e, ImageView result) {
                        if (e != null) {
                            Ion.with(mBackground)
                                    .placeholder(prevDrawable)
                                    .crossfade(true)
                                    .smartSize(true)
                                    .load("android.resource://" + getActivity().getPackageName() + "/" + R.drawable.no_cover_blurred);
                        }
                    }
                });
    }

    @Subscribe
    public void onAccessPointStateChanged(AccessPointStateEvent event) {
        setChatButtonVisibility(event.getState() == ENABLED);
    }

    @Subscribe
    @Override
    public void onSongChanged(SongChangedEvent event) {
        super.onSongChanged(event);
    }

    @Subscribe
    @Override
    public void onPlaybackStarted(PlaybackStartedEvent event) {
        super.onPlaybackStarted(event);
    }

    @Subscribe
    @Override
    public void onPlaybackPaused(PlaybackPausedEvent event) {
        super.onPlaybackPaused(event);
    }

    @Subscribe
    @Override
    public void onSongPlaying(SongPlayingEvent event) {
        super.onSongPlaying(event);
    }

    @Subscribe
    @Override
    public void onQueueShuffled(QueueShuffledEvent event) {
        super.onQueueShuffled(event);
    }

    @Subscribe
    @Override
    public void onRepeatStateChanged(RepeatStateChangedEvent event) {
        super.onRepeatStateChanged(event);
    }

    class PlayerProgressOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        PlayerProgressOnSeekBarChangeListener() {}

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                player.seekTo(PlayerUtils.convertSeekBarToProgress(progress));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
