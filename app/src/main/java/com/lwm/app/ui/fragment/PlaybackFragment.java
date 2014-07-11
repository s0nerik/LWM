package com.lwm.app.ui.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.enrique.stackblur.StackBlurManager;
import com.lwm.app.R;
import com.lwm.app.ui.async.RemoteAlbumArtAsyncGetter;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public abstract class PlaybackFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    public static final int SEEK_BAR_MAX = 100;
    public static final int SEEK_BAR_UPDATE_INTERVAL = 1000;
    public static final int BLUR_RADIUS = 50;
    private static Bitmap noCover;

    private TextView currentTime;
    private TextView duration;
    private SeekBar seekBar;
    private ImageView albumArt;
    private ImageView background;

    // Playback control buttons
    private ImageView playPauseButton;
    private ImageView shuffleButton;
    private ImageView repeatButton;

    private Drawable[] drawables;
    private TransitionDrawable transitionDrawable;

    private int screenWidth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playback, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        duration = (TextView) view.findViewById(R.id.fragment_playback_last_position);
        currentTime = (TextView) view.findViewById(R.id.fragment_playback_now_position);
        seekBar = (SeekBar) view.findViewById(R.id.fragment_playback_seekBar);
        albumArt = (ImageView) view.findViewById(R.id.fragment_playback_cover);

        playPauseButton = (ImageView) view.findViewById(R.id.fragment_playback_play_pause);
        ImageView nextButton = (ImageView) view.findViewById(R.id.fragment_playback_next);
        ImageView prevButton = (ImageView) view.findViewById(R.id.fragment_playback_prev);
        shuffleButton = (ImageView) view.findViewById(R.id.fragment_playback_shuffle_button);
        repeatButton = (ImageView) view.findViewById(R.id.fragment_playback_repeat_button);

        background = (ImageView) view.findViewById(R.id.fragment_playback_background);

        noCover = BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.no_cover);

        playPauseButton.setOnTouchListener(onControlButtonTouchListener);
        nextButton.setOnTouchListener(onControlButtonTouchListener);
        prevButton.setOnTouchListener(onControlButtonTouchListener);
        shuffleButton.setOnTouchListener(onControlButtonTouchListener);
        repeatButton.setOnTouchListener(onControlButtonTouchListener);

        drawables = new Drawable[]{background.getDrawable(), background.getDrawable()};
        transitionDrawable = new TransitionDrawable(drawables);
        transitionDrawable.setCrossFadeEnabled(true);

        seekBar.setMax(SEEK_BAR_MAX);
        seekBar.setOnSeekBarChangeListener(this);
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime.setText(currentTime);
    }

    public void setDuration(String duration) {
        this.duration.setText(duration);
    }

    public void setSeekBarPosition(int percents) {
        this.seekBar.setProgress(percents);
    }

    public void setAlbumArtFromUri(Uri uri){
        Picasso.with(getActivity())
                .load(uri.toString())
                .resize(screenWidth, screenWidth)
                .placeholder(R.drawable.no_cover)
                .centerCrop()
                .into(albumArt);
    }

    public void setRemoteAlbumArt(){
        RemoteAlbumArtAsyncGetter remoteAlbumArtAsyncGetter = new RemoteAlbumArtAsyncGetter(getActivity(), albumArt, background);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
            remoteAlbumArtAsyncGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            remoteAlbumArtAsyncGetter.execute();
        }
    }

    public void setBackgroundImageUri(Uri uri){
        BackgroundChanger backgroundChanger = new BackgroundChanger(getActivity(), background);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
            backgroundChanger.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri);
        } else {
            backgroundChanger.execute(uri);
        }
    }

    public void setPlayButton(boolean playing){
        if(playing){
            playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
        }else{
            playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        }
    }

    public void setShuffleButton(boolean enabled){
        if(enabled){
            shuffleButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_shuffle_active));
        }else{
            shuffleButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_shuffle));
        }
    }

    public void setRepeatButton(boolean enabled){
        if(enabled){
            repeatButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_active));
        }else{
            repeatButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    private class BackgroundChanger extends AsyncTask<Uri, Void, Void> {
        private Context context;
        private ImageView bg;
        private Bitmap bitmap;
        private BitmapDrawable newDrawable;

        public BackgroundChanger(Context context, ImageView bg){
            this.context = context;
            this.bg = bg;
        }

        @Override
        protected Void doInBackground(Uri... uri) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri[0]);
                assert bitmap != null : "bitmap == null";
                if(bitmap != null) {
                    bitmap = new StackBlurManager(bitmap).processNatively(BLUR_RADIUS);
                }else{
                    bitmap = new StackBlurManager(noCover).processNatively(BLUR_RADIUS);
                }
            } catch (IOException e) {
                bitmap = new StackBlurManager(noCover).processNatively(BLUR_RADIUS);
            }
            try{
                newDrawable = new BitmapDrawable(getResources(), bitmap);
            }catch (IllegalStateException ignored){}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(newDrawable != null){
                Drawable oldDrawable = bg.getDrawable();

                if(oldDrawable instanceof TransitionDrawable){
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

    View.OnTouchListener onControlButtonTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                //Button Pressed
                view.setBackgroundColor(Color.parseColor("#33ffffff"));
            }
            if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                //finger was lifted
                view.setBackgroundColor(Color.TRANSPARENT);
            }
            return false;
        }
    };

}
