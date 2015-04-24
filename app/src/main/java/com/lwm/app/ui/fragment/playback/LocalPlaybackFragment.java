package com.lwm.app.ui.fragment.playback;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;
import com.lwm.app.R;
import com.lwm.app.events.access_point.AccessPointStateEvent;
import com.lwm.app.events.player.RepeatStateChangedEvent;
import com.lwm.app.events.player.playback.PlaybackPausedEvent;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.player.playback.SongChangedEvent;
import com.lwm.app.events.player.playback.SongPlayingEvent;
import com.lwm.app.events.player.queue.QueueShuffledEvent;
import com.lwm.app.helper.wifi.WifiAP;
import com.lwm.app.model.Song;
import com.lwm.app.player.BasePlayer;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.ui.Blur;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.OnClick;

import static com.lwm.app.events.access_point.AccessPointStateEvent.State.ENABLED;

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
        mSeekBar.setOnSeekBarChangeListener(new PlayerProgressOnSeekBarChangeListener());
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

    @OnClick({R.id.btnPlayPause, R.id.btnNext, R.id.btnPrev, R.id.btnShuffle, R.id.btnRepeat})
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
}