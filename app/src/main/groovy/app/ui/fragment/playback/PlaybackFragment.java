package app.ui.fragment.playback;

import android.content.res.Resources;
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
import android.widget.TextView;

import com.arasthel.swissknife.SwissKnife;
import com.arasthel.swissknife.annotations.InjectView;
import com.lwm.app.R;
import com.nvanbenschoten.motion.ParallaxImageView;

import javax.inject.Inject;

import app.Utils;
import app.events.player.RepeatStateChangedEvent;
import app.events.player.playback.PlaybackPausedEvent;
import app.events.player.playback.PlaybackStartedEvent;
import app.events.player.playback.SongChangedEvent;
import app.events.player.playback.SongPlayingEvent;
import app.events.player.queue.QueueShuffledEvent;
import app.model.Song;
import app.player.BasePlayer;
import app.player.PlayerUtils;
import app.ui.async.RemoteAlbumArtAsyncGetter;
import app.ui.base.DaggerOttoOnResumeFragment;
import ru.noties.debug.Debug;

public abstract class PlaybackFragment extends DaggerOttoOnResumeFragment {

    private BasePlayer player;

    @Inject
    Resources resources;

    @InjectView(R.id.background)
    ParallaxImageView mBackground;
    @InjectView(R.id.cover)
    ImageView mCover;
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
    @InjectView(R.id.btnPrevIcon)
    ImageView mBtnPrevIcon;
    @InjectView(R.id.btnPlayPauseIcon)
    ImageView mBtnPlayPauseIcon;
    @InjectView(R.id.btnNextIcon)
    ImageView mBtnNextIcon;
    @InjectView(R.id.btnRepeatIcon)
    ImageView mBtnRepeatIcon;
    @InjectView(R.id.controls)
    LinearLayout mControls;
    @InjectView(R.id.bottomBar)
    LinearLayout mBottomBar;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    protected abstract BasePlayer getPlayer();
    protected abstract void setCover(final Song song);
    protected abstract void setBackground(final Song song);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        player = getPlayer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playback, container, false);
        SwissKnife.inject(this, v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
        mBackground.registerSensorManager();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBackground.unregisterSensorManager();
    }

    protected void onSongPlaying(SongPlayingEvent event) {
        mSeekBar.setProgress(PlayerUtils.calculateProgressForSeekBar(event.getProgress()));
        mCurrentTime.setText(player.getCurrentPositionInMinutes());
    }

    protected void onSongChanged(SongChangedEvent event) {
        setSongInfo(event.getSong());
    }

    protected void onPlaybackStarted(PlaybackStartedEvent event) {
        Debug.d("onPlaybackStarted");
        setPlayButton(player.isPlaying());
    }

    protected void onPlaybackPaused(PlaybackPausedEvent event) {
        Debug.d("onPlaybackPaused");
        setPlayButton(player.isPlaying());
    }

    protected void onQueueShuffled(QueueShuffledEvent event) {
        setShuffleButton(player.isShuffle());
    }

    protected void onRepeatStateChanged(RepeatStateChangedEvent event) {
        setRepeatButton(player.isRepeat());
    }

    protected void setSongInfo(final Song song) {
        if (song == null) return;
        mToolbar.setTitle(song.getTitle());
        mToolbar.setSubtitle(song.getArtist());

        mSeekBar.setMax(PlayerUtils.calculateProgressForSeekBar(song.getDuration()));
        mEndTime.setText(song.getDurationString());

        setCover(song);
        setBackground(song);
    }

    protected void setAlbumArtFromUri(Uri uri) {
        Utils.setAlbumArtFromUri(getActivity(), mCover, uri);
    }

    public void setRemoteAlbumArt() {
        RemoteAlbumArtAsyncGetter remoteAlbumArtAsyncGetter = new RemoteAlbumArtAsyncGetter(getActivity(), mCover, mBackground);
        remoteAlbumArtAsyncGetter.executeWithThreadPoolExecutor();
    }


    private void setPlayButton(boolean playing) {
        if (playing) {
            mBtnPlayPauseIcon.setImageResource(R.drawable.ic_av_pause);
        } else {
            mBtnPlayPauseIcon.setImageResource(R.drawable.ic_av_play_arrow);
        }
    }

    private void setShuffleButton(boolean enabled) {
        mBtnShuffleIcon.setColorFilter(resources.getColor(enabled? R.color.primary : android.R.color.white));
    }

    private void setRepeatButton(boolean enabled) {
        mBtnRepeatIcon.setColorFilter(resources.getColor(enabled? R.color.primary : android.R.color.white));
    }

    private void initView() {
        setSongInfo(player.getCurrentSong());
        mCurrentTime.setText(player.getCurrentPositionInMinutes());
        mSeekBar.setProgress(PlayerUtils.calculateProgressForSeekBar(player.getCurrentPosition()));
        setPlayButton(player.isPlaying());
        setShuffleButton(player.isShuffle());
        setRepeatButton(player.isRepeat());
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
