package app.player

import android.net.Uri
import android.support.annotation.NonNull
import app.Utils
import com.google.android.exoplayer.ExoPlaybackException
import com.google.android.exoplayer.ExoPlayer
import com.google.android.exoplayer.TrackRenderer
import groovy.transform.CompileStatic
import ru.noties.debug.Debug
import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject

import static com.google.android.exoplayer.ExoPlayer.*

@CompileStatic
abstract class RxExoPlayer {

    protected ExoPlayer innerPlayer
    protected TrackRenderer currentRenderer

    protected SerializedSubject<PlayerEvent, PlayerEvent> playerSubject = PublishSubject.create().toSerialized()

    protected Observable readyObservable = playerSubject.filter { it == PlayerEvent.READY }
    protected Observable preparingObservable = playerSubject.filter { it == PlayerEvent.PREPARING }
    protected Observable bufferingObservable = playerSubject.filter { it == PlayerEvent.BUFFERING }
    protected Observable startedObservable = playerSubject.filter { it == PlayerEvent.STARTED }
    protected Observable pausedObservable = playerSubject.filter { it == PlayerEvent.PAUSED }
    protected Observable endedObservable = playerSubject.filter { it == PlayerEvent.ENDED }
    protected Observable idleObservable = playerSubject.filter { it == PlayerEvent.IDLE }

    protected SerializedSubject<ExoPlaybackException, ExoPlaybackException> errorSubject = PublishSubject.create().toSerialized()

    protected int lastState = STATE_IDLE

    enum PlayerEvent {
        READY, PREPARING, BUFFERING, STARTED, PAUSED, ENDED, IDLE
    }

    private Listener listener = [
            onPlayerStateChanged    : { boolean playWhenReady, int playbackState ->
                switch (playbackState) {
                    case STATE_ENDED:
                        playerSubject.onNext PlayerEvent.ENDED
                        break
                    case STATE_IDLE:
                        playerSubject.onNext PlayerEvent.IDLE
                        break
                    case STATE_PREPARING:
                        playerSubject.onNext PlayerEvent.PREPARING
                        break
                    case STATE_BUFFERING:
                        playerSubject.onNext PlayerEvent.BUFFERING
                        break
                    case STATE_READY:
                        playerSubject.onNext PlayerEvent.READY
                        if (playWhenReady) {
                            playerSubject.onNext PlayerEvent.STARTED
                        }
                        break
                }
                lastState = playbackState

                Debug.d Utils.getConstantName(ExoPlayer, playbackState)
            },
            onPlayWhenReadyCommitted: {
                if (!innerPlayer.playWhenReady)
                    playerSubject.onNext PlayerEvent.PAUSED
            },
            onPlayerError           : { ExoPlaybackException e ->
                playerSubject.onError e
                errorSubject.onNext e
            }
    ] as Listener

    RxExoPlayer() {
        innerPlayer = Factory.newInstance 1
//        innerPlayer = Factory.newInstance 1, 30 * 1000, 60 * 1000
        innerPlayer.addListener listener
        innerPlayer.playWhenReady = false
    }

    // region Player state
    boolean isPaused() { !innerPlayer.playWhenReady }

    boolean isPlaying() { ready && !paused }

    boolean isReady() { innerPlayer.playbackState == STATE_READY }

    int getCurrentPosition() { innerPlayer.currentPosition }
    // endregion

    protected abstract TrackRenderer getRenderer(Uri uri)

    /**
     * Start playback
     * @return true if playback started successfully and false means that error has occurred during playback startup.
     */
    Observable start() {
        Observable.defer {
            if (innerPlayer.playWhenReady) {
                Observable.empty()
            } else {
                def started = startedObservable.take(1)

                started.doOnSubscribe { innerPlayer.playWhenReady = true }
                       .ignoreElements()
            }
        }
    }

    /**
     * Restart playback from the beginning
     * @return true if playback started successfully and false means that error has occurred during playback startup.
     */
    Observable restart() {
        Observable.concat seekTo(0), start()
    }

    /**
     * Pause playback
     * @return true if playback paused successfully and false means that error has occurred during playback pausing.
     */
    Observable pause() {
        Observable.defer {
            if (!innerPlayer.playWhenReady) {
                Observable.empty()
            } else {
                def paused = pausedObservable.take(1)

                paused.doOnSubscribe { innerPlayer.playWhenReady = false }
                      .ignoreElements()
            }
        }
    }

    Observable setPaused(boolean flag) {
        if (flag)
            pause()
        else
            start()
    }

    Observable togglePause() { setPaused !paused }

    /**
     * Stop playback
     * @return true if playback stopped successfully and false means that error has occurred during playback stopping.
     */
    Observable stop() {
        Observable.defer {
            if (innerPlayer.playbackState == STATE_IDLE) {
                Observable.empty()
            } else {
                def idle = idleObservable.take(1)

                idle.doOnSubscribe { innerPlayer.stop() }
                    .ignoreElements()
            }
        }
    }

    /**
     * Prepare a new Uri for playback
     * @return true if a new stream for playback is prepared successfully and false means that error has occurred during preparing.
     */
    Observable prepare(@NonNull Uri uri) {
        Observable.defer {
            def preparing = preparingObservable.take(1)
            def ready = readyObservable.take(1)

            Observable.concat(preparing, ready)
                      .doOnSubscribe {
                currentRenderer = getRenderer(uri)
                innerPlayer.prepare currentRenderer
            }
                      .ignoreElements()
        }
    }

    /**
     * Seek to position
     * @return true if player successfully sought to the desired position and false means that error has occurred during seeking.
     */
    Observable seekTo(int msec) {
        Observable.defer {
            if (lastState == STATE_IDLE || lastState == STATE_PREPARING || innerPlayer.currentPosition == msec) {
                Observable.empty()
            } else {
                def buffering = bufferingObservable.take(1)
                def ready = readyObservable.take(1)

                Observable.concat(buffering, ready)
                          .doOnSubscribe { innerPlayer.seekTo msec }
                          .ignoreElements()
            }
        }
    }

    /**
     * Seek to the beginning
     * @return true if player successfully sought to the desired position and false means that error has occurred during seeking.
     */
    Observable reset() {
        Observable.defer {
            Observable.concat pause(), seekTo(0), stop()
//            if (innerPlayer.playbackState == STATE_IDLE) {
//                pause()
//            } else {
//                Observable.concat pause(), seekTo(0), stop()
//            }
        }
    }
}