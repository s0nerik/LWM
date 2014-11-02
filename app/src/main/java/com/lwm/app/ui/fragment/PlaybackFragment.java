package com.lwm.app.ui.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.lwm.app.R;
import com.lwm.app.SupportAsyncTask;
import com.lwm.app.Utils;
import com.lwm.app.ui.async.RemoteAlbumArtAsyncGetter;
import com.lwm.app.ui.base.DaggerFragment;
import com.lwm.app.ui.custom_view.SquareWidthImageView;
import com.squareup.otto.Bus;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTouch;

public abstract class PlaybackFragment extends DaggerFragment implements SeekBar.OnSeekBarChangeListener {

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

    public static final int BLUR_RADIUS = 50;

    private Drawable[] drawables;
    private TransitionDrawable transitionDrawable;

    protected MediaPlayer player;

    @Inject
    Bus bus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
    }

    @Override
    public void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playback, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        drawables = new Drawable[]{mBackground.getDrawable(), mBackground.getDrawable()};
        transitionDrawable = new TransitionDrawable(drawables);
        transitionDrawable.setCrossFadeEnabled(true);

//        seekBar.setMax(SEEK_BAR_MAX);
//        seekBar.setOnSeekBarChangeListener(this);
    }

    public void setCurrentTime(String currentTime) {
        mCurrentTime.setText(currentTime);
    }

    public void setDuration(String duration) {
        mEndTime.setText(duration);
    }

    public void setSeekBarPosition(int percents) {
        mSeekBar.setProgress(percents);
    }

    public void setAlbumArtFromUri(Uri uri) {
        Utils.setAlbumArtFromUri(getActivity(), mCover, uri);
    }

    public void setRemoteAlbumArt() {
        RemoteAlbumArtAsyncGetter remoteAlbumArtAsyncGetter = new RemoteAlbumArtAsyncGetter(getActivity(), mCover, mBackground);
        remoteAlbumArtAsyncGetter.executeWithThreadPoolExecutor();
    }

    public void setBackgroundImageUri(Uri uri) {
        BackgroundChanger backgroundChanger = new BackgroundChanger(getActivity(), mBackground);
        backgroundChanger.executeWithThreadPoolExecutor(uri);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private class BackgroundChanger extends SupportAsyncTask<Uri, Void, Drawable> {
        private Context context;
        private ImageView bg;
        private Bitmap bitmap;

        public BackgroundChanger(Context context, ImageView bg) {
            this.context = context;
            this.bg = bg;
        }

        @Override
        protected Drawable doInBackground(Uri... uri) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri[0]);
//                assert bitmap != null : "bitmap == null";
                if (bitmap != null) {
                    bitmap = new StackBlurManager(bitmap).processNatively(BLUR_RADIUS);
                } else {
                    bitmap = ((BitmapDrawable) bg.getDrawable()).getBitmap();
                }
            } catch (IOException e) {
                bitmap = ((BitmapDrawable) bg.getDrawable()).getBitmap();
            }
            try {
                return new BitmapDrawable(getResources(), bitmap);
            } catch (IllegalStateException ignored) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable newDrawable) {
            if (newDrawable != null) {
                Drawable oldDrawable = bg.getDrawable();

                if (oldDrawable instanceof TransitionDrawable) {
                    oldDrawable = ((TransitionDrawable) oldDrawable).getDrawable(1);
                }

                drawables[0] = oldDrawable;
                drawables[1] = newDrawable;
                transitionDrawable = new TransitionDrawable(drawables);

                bg.setImageDrawable(transitionDrawable);
                transitionDrawable.startTransition(1000);
            }
        }
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

}
