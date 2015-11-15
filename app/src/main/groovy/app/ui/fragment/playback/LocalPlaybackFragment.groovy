package app.ui.fragment.playback
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.annotation.Nullable
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import app.R
import app.Utils
import app.commands.ChangePauseStateCommand
import app.commands.PlaySongAtPositionCommand
import app.commands.SeekToCommand
import app.events.player.RepeatStateChangedEvent
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongChangedEvent
import app.events.player.playback.SongPlayingEvent
import app.events.player.queue.QueueShuffledEvent
import app.model.Song
import app.player.BasePlayer
import app.player.LocalPlayer
import app.player.PlayerUtils
import app.ui.Blurer
import com.github.s0nerik.betterknife.annotations.OnClick
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import rx.Observable
import rx.Subscriber

import javax.inject.Inject

import static app.commands.PlaySongAtPositionCommand.PositionType.*

@CompileStatic
class LocalPlaybackFragment extends PlaybackFragment {

    @Inject
    @PackageScope
    LocalPlayer player

    @Inject
    @PackageScope
    Utils utils

    @Inject
    @PackageScope
    Bus bus

//    @Inject
//    @PackageScope
//    WifiAP wifiAP

    // TODO: Remove this when error with non-existing blurer property problem in LocalPlaybackFragment is gone
    @Inject
    @PackageScope
    Blurer blurer

    @Inject
    @PackageScope
    WindowManager windowManager

    @Inject
    @PackageScope
    ContentResolver contentResolver

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
//        chatButtonVisibility = wifiAP.enabled
    }

    private void setChatButtonVisibility(boolean show) {
        chatButton.visibility = show ? View.VISIBLE : View.GONE
    }

    @OnClick([R.id.btnPlayPause, R.id.btnNext, R.id.btnPrev, R.id.btnShuffle, R.id.btnRepeat])
    void onClickControls(View btn) {
        switch (btn.getId()) {
            case R.id.btnNext:
                bus.post new PlaySongAtPositionCommand(NEXT)
                break;
            case R.id.btnPrev:
                bus.post new PlaySongAtPositionCommand(PREVIOS)
                break;
            case R.id.btnPlayPause:
                bus.post new ChangePauseStateCommand(!player.paused)
                break
            case R.id.btnShuffle:
                player.shuffleQueueExceptPlayed()
                break
            case R.id.btnRepeat:
                player.repeat = !player.repeat
                break
        }
    }

    @Override
    protected BasePlayer getPlayer() { player }

    @Override
    protected Observable<Bitmap> getCoverBitmap(Song song) {
        Observable.create({ Subscriber<Bitmap> subscriber ->
            Bitmap bmp = null
            try {
                def is = contentResolver.openInputStream song.albumArtUri
                is.withStream {
                    bmp = BitmapFactory.decodeStream(it)
                }
            } catch (ignored) {}

            bmp = bmp ?: utils.noCoverBitmap

            subscriber.onNext bmp
            subscriber.onCompleted()
        } as Observable.OnSubscribe<Bitmap>)
    }

//    @Subscribe
//    void onAccessPointStateChanged(MusicStationStateEvent event) {
//        chatButtonVisibility = event.state == ENABLED
//    }

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
                bus.post new SeekToCommand(PlayerUtils.convertSeekBarToProgress(progress))
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
