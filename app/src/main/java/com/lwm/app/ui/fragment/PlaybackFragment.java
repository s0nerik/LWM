package com.lwm.app.ui.fragment;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.andexert.library.RippleView;
import com.danh32.fontify.TextView;
import com.enrique.stackblur.StackBlurManager;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;
import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.player.playback.SongPlayingEvent;
import com.lwm.app.events.player.service.CurrentSongAvailableEvent;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.PlayerUtils;
import com.lwm.app.ui.async.RemoteAlbumArtAsyncGetter;
import com.lwm.app.ui.custom_view.SquareWidthImageView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public abstract class PlaybackFragment extends DaggerOttoFragment {

    public static final int BLUR_RADIUS = 50;

    @Inject
    LocalPlayer player;

    @InjectView(R.id.background)
    ImageView mBackground;
    @InjectView(R.id.cover)
    SquareWidthImageView mCover;
    @InjectView(R.id.albumArtLayout)
    FrameLayout mAlbumArtLayout;
    @InjectView(R.id.currentTime)
    TextView mCurrentTime;
    @InjectView(R.id.endTime)
    TextView mEndTime;
    @InjectView(R.id.seekBar)
    SeekBar mSeekBar;
    @InjectView(R.id.btnShuffleIcon)
    ImageView mBtnShuffleIcon;
    @InjectView(R.id.btnShuffle)
    RippleView mBtnShuffle;
    @InjectView(R.id.btnPrevIcon)
    ImageView mBtnPrevIcon;
    @InjectView(R.id.btnPrev)
    RippleView mBtnPrev;
    @InjectView(R.id.btnPlayPauseIcon)
    ImageView mBtnPlayPauseIcon;
    @InjectView(R.id.btnPlayPause)
    RippleView mBtnPlayPause;
    @InjectView(R.id.btnNextIcon)
    ImageView mBtnNextIcon;
    @InjectView(R.id.btnNext)
    RippleView mBtnNext;
    @InjectView(R.id.btnRepeatIcon)
    ImageView mBtnRepeatIcon;
    @InjectView(R.id.btnRepeat)
    RippleView mBtnRepeat;
    @InjectView(R.id.controls)
    LinearLayout mControls;
    @InjectView(R.id.bottomBar)
    LinearLayout mBottomBar;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playback, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    protected void onSongPlaying(SongPlayingEvent event) {
        mSeekBar.setProgress(PlayerUtils.calculateProgressForSeekBar(event.getProgress()));
        mCurrentTime.setText(player.getCurrentPositionInMinutes());
    }

    protected void onCurrentSongAvailable(CurrentSongAvailableEvent event) {
        setSongInfo(event.getSong());
    }

    protected void onPlaybackStarted(PlaybackStartedEvent event) {
        setSongInfo(event.getSong());
    }

    protected void setSongInfo(final Song song) {
        mToolbar.setTitle(song.getTitle());
        mToolbar.setSubtitle(song.getArtist());

        mSeekBar.setMax(PlayerUtils.calculateProgressForSeekBar(song.getDuration()));
        mEndTime.setText(song.getDurationString());

        Ion.with(mCover)
                .crossfade()
                .placeholder(R.drawable.no_cover)
                .error(R.drawable.no_cover)
                .smartSize(true)
                .load(song.getAlbumArtUri().toString());

        Ion.with(mBackground)
                .placeholder(R.drawable.no_cover_blurred)
                .error(R.drawable.no_cover_blurred)
                .crossfade()
                .smartSize(true)
                .transform(new Transform() {
                    @Override
                    public Bitmap transform(Bitmap b) {
                        return new StackBlurManager(b).processNatively(BLUR_RADIUS);
                    }

                    @Override
                    public String key() {
                        return song.getAlbumArtUri().toString();
                    }
                })
                .load(song.getAlbumArtUri().toString());
    }

    protected void setAlbumArtFromUri(Uri uri) {
        Utils.setAlbumArtFromUri(getActivity(), mCover, uri);
    }

    public void setRemoteAlbumArt() {
        RemoteAlbumArtAsyncGetter remoteAlbumArtAsyncGetter = new RemoteAlbumArtAsyncGetter(getActivity(), mCover, mBackground);
        remoteAlbumArtAsyncGetter.executeWithThreadPoolExecutor();
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
                setPlayButton(player.isPlaying());
                break;
            case R.id.btnShuffle:
                player.shuffleQueueExceptPlayed();
                setShuffleButton(player.isShuffle());
                break;
            case R.id.btnRepeat:
                player.setRepeat(!player.isRepeat());
                setRepeatButton(player.isRepeat());
                break;
        }
    }

    private void setPlayButton(boolean playing) {
        if (playing) {
            mBtnPlayPauseIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
        } else {
            mBtnPlayPauseIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        }
    }

    private void setShuffleButton(boolean enabled) {
        if (enabled) {
            mBtnShuffleIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_shuffle_active));
        } else {
            mBtnShuffleIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_shuffle));
        }
    }

    private void setRepeatButton(boolean enabled) {
        if (enabled) {
            mBtnRepeatIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_active));
        } else {
            mBtnRepeatIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat));
        }
    }

    protected class PlayerProgressOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

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
