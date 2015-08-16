package app.service

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder

import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

import app.Injector
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.control.ChangeSongEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.events.server.AllClientsReadyEvent
import app.events.server.PauseClientsEvent
import app.events.server.StartClientsEvent
import app.player.LocalPlayer
import app.receiver.MediaButtonIntentReceiver
import app.ui.notification.NowPlayingNotification

import static app.events.player.playback.control.ChangeSongEvent.Type.*

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
        Injector.inject(this)

        bus.register(this)

        mediaButtonsReceiver = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getCanonicalName())
        audioManager.registerMediaButtonEventReceiver(mediaButtonsReceiver)
    }

    @Override
    void onDestroy() {
        stopForeground(true)

        if (player.isPlaying()) {
            player.pause()
        }

        bus.unregister(this)

        audioManager.unregisterMediaButtonEventReceiver(mediaButtonsReceiver)
    }

    @Override
    IBinder onBind(Intent intent) {
        return null
    }

    @Override
    int onStartCommand(Intent intent, int flags, int startId) {
        if (player.hasCurrentSong()) {
            makeForeground(player.isPlaying())
        }
        return START_NOT_STICKY
    }

    private void makeForeground(boolean isPlaying) {
        startForeground(1337,
                new NowPlayingNotification(player.getCurrentSong()).create(isPlaying)
        )
    }

    @Produce
    CurrentSongAvailableEvent produceCurrentSong() {
        return new CurrentSongAvailableEvent(player.getCurrentSong())
    }

    @Subscribe
    void onChangeSongEvent(ChangeSongEvent event) {
        switch (event.type) {
            case NEXT:
                player.nextSong()
                break
            case PREV:
                player.prevSong()
                break
            case PLAY:
                if (!player.playing) {
                    player.unpause()
                }
                break
            case PAUSE:
                if (player.playing) {
                    player.pause()
                }
                break
            case TOGGLE_PAUSE:
                player.togglePause()
                break
        }
    }

    @Subscribe
    void onPlaybackStarted(PlaybackStartedEvent event) {
        makeForeground(true)
    }

    @Subscribe
    void onPlaybackPaused(PlaybackPausedEvent event) {
        makeForeground(false)
    }

    @Subscribe
    void allClientsReady(AllClientsReadyEvent event) {
        player.unpause()
    }

    @Subscribe
    void onStartClients(StartClientsEvent event) {
        player.unpause()
    }

    @Subscribe
    void onPauseClients(PauseClientsEvent event) {
        player.pause()
    }

}
