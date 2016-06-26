package app.ui.fragment.playback

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.annotation.Nullable
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import app.App
import app.R
import app.Utils
import app.commands.ChangePauseStateCommand
import app.commands.PlaySongAtPositionCommand
import app.commands.SeekToCommand
import app.models.Song
import app.players.BasePlayer
import app.players.LocalPlayer
import app.players.PlayerUtils
import app.rx.RxBus
import app.ui.Blurer
import com.github.s0nerik.betterknife.annotations.OnClick
import groovy.transform.CompileStatic
import rx.Observable
import rx.Subscriber

import javax.inject.Inject

import static app.commands.PlaySongAtPositionCommand.PositionType.NEXT
import static app.commands.PlaySongAtPositionCommand.PositionType.PREVIOUS

@CompileStatic
class LocalPlaybackFragment extends PlaybackFragment {

    @Inject
    protected LocalPlayer player

    @Inject
    protected Utils utils

//    @Inject
//    @PackageScope
//    WifiAP wifiAP

    // TODO: Remove this when error with non-existing blurer property problem in LocalPlaybackFragment is gone
    @Inject
    protected Blurer blurer

    @Inject
    protected WindowManager windowManager

    @Inject
    protected ContentResolver contentResolver

    private View chatButton

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        App.get().inject(this)
    }

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
                RxBus.post new PlaySongAtPositionCommand(NEXT)
                break;
            case R.id.btnPrev:
                RxBus.post new PlaySongAtPositionCommand(PREVIOUS)
                break;
            case R.id.btnPlayPause:
                RxBus.post new ChangePauseStateCommand(!player.paused)
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

    class PlayerProgressOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        PlayerProgressOnSeekBarChangeListener() {}

        @Override
        void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                RxBus.post new SeekToCommand(PlayerUtils.convertSeekBarToProgress(progress))
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
