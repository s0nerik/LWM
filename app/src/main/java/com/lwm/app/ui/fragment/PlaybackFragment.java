package com.lwm.app.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.danh32.fontify.TextView;
import com.enrique.stackblur.StackBlurManager;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;
import com.lwm.app.R;
import com.lwm.app.Utils;
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
import butterknife.OnTouch;

public abstract class PlaybackFragment extends DaggerOttoFragment {

    public static final int BLUR_RADIUS = 50;

    @Inject
    LocalPlayer player;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
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
    @InjectView(R.id.btnShuffle)
    ImageView mBtnShuffle;
    @InjectView(R.id.btnPrev)
    ImageView mBtnPrev;
    @InjectView(R.id.btnPlayPause)
    ImageView mBtnPlayPause;
    @InjectView(R.id.btnNext)
    ImageView mBtnNext;
    @InjectView(R.id.btnRepeat)
    ImageView mBtnRepeat;
    @InjectView(R.id.controls)
    LinearLayout mControls;
    @InjectView(R.id.bottomBar)
    LinearLayout mBottomBar;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;

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

    @OnTouch({R.id.btnPlayPause, R.id.btnNext, R.id.btnPrev, R.id.btnShuffle, R.id.btnRepeat})
    public boolean onTouchControls(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            //Button Pressed
            view.setBackgroundColor(Color.parseColor("#33ffffff"));
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            //finger was lifted
            view.setBackgroundColor(Color.TRANSPARENT);
        }
        return true;
    }

    protected class PlayerProgressOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                player.seekTo(PlayerUtils.convertSeekBarToProgress(progress));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

}
