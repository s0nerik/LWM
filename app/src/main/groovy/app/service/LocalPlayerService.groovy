package app.service
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import app.Injector
import app.commands.*
import app.events.player.ReadyToStartPlaybackEvent
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.control.ControlButtonEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.events.server.MusicServerStateChangedEvent
import app.player.LocalPlayer
import app.receiver.MediaButtonIntentReceiver
import app.ui.notification.NowPlayingNotification
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import ru.noties.debug.Debug
import rx.Observable
import rx.android.schedulers.AndroidSchedulers

import javax.inject.Inject
import java.util.concurrent.TimeUnit

import static app.events.player.playback.control.ControlButtonEvent.Type.*
import static app.events.server.MusicServerStateChangedEvent.State.STARTED

@PackageScope(PackageScopeTarget.FIELDS)
@CompileStatic
class LocalPlayerService extends Service {

    @Inject
    Bus bus

    @Inject
    LocalPlayer player

    @Inject
    AudioManager audioManager

    ComponentName mediaButtonsReceiver

    private boolean serverStarted = false

    @Override
    void onCreate() {
        Injector.inject this

        bus.register this

        mediaButtonsReceiver = new ComponentName(packageName, MediaButtonIntentReceiver.canonicalName)
        audioManager.registerMediaButtonEventReceiver mediaButtonsReceiver
    }

    @Override
    void onDestroy() {
        stopForeground true

        player.stop()

        bus.unregister this

        audioManager.unregisterMediaButtonEventReceiver mediaButtonsReceiver
    }

    @Override
    IBinder onBind(Intent intent) { null }

    @Override
    int onStartCommand(Intent intent, int flags, int startId) {
//        makeForeground player.playing
        return START_NOT_STICKY
    }

    private void makeForeground(boolean isPlaying) {
        startForeground 1337, new NowPlayingNotification(player.currentSong).create(isPlaying)
    }

    @Produce
    CurrentSongAvailableEvent produceCurrentSong() { new CurrentSongAvailableEvent(player.currentSong) }

    @Subscribe
    void startPlaybackDelayed(StartPlaybackDelayedCommand cmd) {
        Observable.timer(cmd.startAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                  .subscribe {
                      player.setPaused(false).subscribe {
                          Debug.d "LocalPlayer started playback with delay."
                      }
                  }
    }

    @Subscribe
    void changePauseState(ChangePauseStateCommand cmd) {
        if (serverStarted && !cmd.pause)
            return

        player.setPaused(cmd.pause).subscribe {
            Debug.d "LocalPlayer setPaused: $it"
        }
    }

    @Subscribe
    void seekTo(SeekToCommand cmd) {
        player.seekTo(cmd.position as int).subscribe {
            Debug.d "Player sought to: $it"
        }
    }

    @Subscribe
    void setQueueAndPlayCommand(SetQueueAndPlayCommand cmd) {
        player.queue = cmd.queue
        if (cmd.shuffle) player.shuffleQueue()

        Observable.concat(player.prepare(cmd.position), player.start())
                .subscribe {
            Debug.d "LocalPlayer prepared and started playback"
        }
    }

    @Subscribe
    void playSongAtPosition(PlaySongAtPositionCommand cmd) {
        switch (cmd.positionType) {
            case PlaySongAtPositionCommand.PositionType.NEXT:
                player.nextSong()
                        .concatWith(player.start())
                        .subscribe()
                break
            case PlaySongAtPositionCommand.PositionType.PREVIOS:
                player.prevSong()
                        .concatWith(player.start())
                        .subscribe()
                break
            case PlaySongAtPositionCommand.PositionType.EXACT:
                player.prepare(cmd.position)
                        .concatWith(player.start())
                        .subscribe()
                break
        }
    }

    @Subscribe
    void enqueue(EnqueueCommand cmd) {
        player.addToQueue cmd.playlist
    }

    @Subscribe
    void onMusicServerStateChanged(MusicServerStateChangedEvent event) {
        serverStarted = event.state == STARTED
    }

    @Subscribe
    void onReadyToStartPlayback(ReadyToStartPlaybackEvent event) {
        if (serverStarted) {
            bus.post new CurrentSongAvailableEvent(event.song)
            bus.post new PrepareClientsCommand(event.position)
        } else {
            player.start().subscribe {
                Debug.d "LocalPlayer started playback."
            }
        }
    }

    @Subscribe
    void onControlButton(ControlButtonEvent event) {
        Debug.d event as String
        switch (event.type) {
            case NEXT:
                player.nextSong()
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                    "LocalPlayer moved to next song"
                }
                break
            case PREV:
                player.prevSong().subscribe {
                    "LocalPlayer moved to previous song"
                }
                break
            case PLAY:
            case PAUSE:
            case TOGGLE_PAUSE:
                player.togglePause().subscribe {
                    "LocalPlayer toggled pause"
                }
                break
        }
    }

    @Subscribe
    void onPlaybackStarted(PlaybackStartedEvent event) {
        makeForeground true
    }

    @Subscribe
    void onPlaybackPaused(PlaybackPausedEvent event) {
        makeForeground false
    }

}
