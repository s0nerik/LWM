package app.service

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import app.Injector
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.control.ControlButtonEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.events.server.AllClientsReadyEvent
import app.events.server.PauseClientsEvent
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

import javax.inject.Inject

import static ControlButtonEvent.Type.*

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
        makeForeground player.playing
        return START_NOT_STICKY
    }

    private void makeForeground(boolean isPlaying) {
        startForeground 1337, new NowPlayingNotification(player.currentSong).create(isPlaying)
    }

    @Produce
    CurrentSongAvailableEvent produceCurrentSong() { new CurrentSongAvailableEvent(player.currentSong) }

    @Subscribe
    void onControlButton(ControlButtonEvent event) {
        Debug.d event as String
        switch (event.type) {
            case NEXT:
                player.nextSong()
                break
            case PREV:
                player.prevSong()
                break
            case PLAY:
            case PAUSE:
            case TOGGLE_PAUSE:
                player.togglePause()
                break
        }
    }

    @Subscribe
    void onPlaybackStarted(PlaybackStartedEvent event) {
        Debug.d()
        makeForeground true
    }

    @Subscribe
    void onPlaybackPaused(PlaybackPausedEvent event) {
        Debug.d()
        makeForeground false
    }

    @Subscribe
    void allClientsReady(AllClientsReadyEvent event) {
        Debug.d()
        player.setPaused false, true
    }

    @Subscribe
    void onPauseClients(PauseClientsEvent event) {
        Debug.d()
        player.setPaused true, true
    }

}
