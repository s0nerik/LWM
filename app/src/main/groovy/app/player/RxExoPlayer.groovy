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
    protected SerializedSubject<ExoPlaybackException, ExoPlaybackException> errorSubject = PublishSubject.create().toSerialized()

    protected int lastState = STATE_IDLE

    enum PlayerEvent {
        READY, STARTED, PAUSED, ENDED, IDLE
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
                        break
                    case STATE_BUFFERING:
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
//                Debug.d "onPlayWhenReadyCommitted: $innerPlayer.playWhenReady"
                if (!innerPlayer.playWhenReady)
                    playerSubject.onNext PlayerEvent.PAUSED
            },
            onPlayerError           : { ExoPlaybackException e ->
                playerSubject.onError e
                errorSubject.onNext e
            }
    ] as Listener

    RxExoPlayer() {
        innerPlayer = Factory.newInstance 1, 10000, 20000
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
        playerSubject.first { it == PlayerEvent.STARTED }
                .ignoreElements()
        .doOnNext {
            Debug.d "RxExoPlayer: start() onNext"
        }
        .doOnCompleted {
            Debug.d "RxExoPlayer: start() onCompleted"
        }
        .doOnSubscribe {
            Debug.d "RxExoPlayer: start() onSubscribe"
            innerPlayer.playWhenReady = true
        }
    }

    /**
     * Restart playback from the beginning
     * @return true if playback started successfully and false means that error has occurred during playback startup.
     */
    Observable restart() {
        Observable.concat reset(), start()
    }

    /**
     * Pause playback
     * @return true if playback paused successfully and false means that error has occurred during playback pausing.
     */
    Observable pause() {
        playerSubject.first { it == PlayerEvent.PAUSED }
                .ignoreElements()
        .doOnNext {
            Debug.d "RxExoPlayer: pause() onNext"
        }
        .doOnCompleted {
            Debug.d "RxExoPlayer: pause() onCompleted"
        }
        .doOnSubscribe {
            Debug.d "RxExoPlayer: pause() onSubscribe"
            innerPlayer.playWhenReady = false
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
        playerSubject.first { it == PlayerEvent.IDLE }
                .ignoreElements()
        .doOnNext {
            Debug.d "RxExoPlayer: stop() onNext"
        }
        .doOnCompleted {
            Debug.d "RxExoPlayer: stop() onCompleted"
        }
        .doOnSubscribe {
            Debug.d "RxExoPlayer: stop() onSubscribe"
            innerPlayer.stop()
        }
    }

    /**
     * Prepare a new Uri for playback
     * @return true if a new stream for playback is prepared successfully and false means that error has occurred during preparing.
     */
    Observable prepare(@NonNull Uri uri) {
        playerSubject.first { it == PlayerEvent.READY }
                .ignoreElements()
                .doOnNext {
            Debug.d "RxExoPlayer: prepare(Uri) onNext"
        }
        .doOnCompleted {
            Debug.d "RxExoPlayer: prepare(Uri) onCompleted"
        }
        .doOnSubscribe {
            Debug.d "RxExoPlayer: prepare(Uri) onSubscribe"
            currentRenderer = getRenderer(uri)
            innerPlayer.prepare currentRenderer
        }
    }

    /**
     * Seek to position
     * @return true if player successfully sought to the desired position and false means that error has occurred during seeking.
     */
    Observable seekTo(int msec) {
        playerSubject.first { it == PlayerEvent.READY }
                .ignoreElements()
        .doOnNext {
            Debug.d "RxExoPlayer: seekTo() onNext"
        }
        .doOnCompleted {
            Debug.d "RxExoPlayer: seekTo() onCompleted"
        }
        .doOnSubscribe {
            Debug.d "RxExoPlayer: seekTo() onSubscribe"
            innerPlayer.seekTo msec
        }
    }

    /**
     * Seek to the beginning
     * @return true if player successfully sought to the desired position and false means that error has occurred during seeking.
     */
    Observable reset() {
        playerSubject.first { it == PlayerEvent.IDLE || it == PlayerEvent.READY }
                .ignoreElements()
        .doOnNext {
            Debug.d "RxExoPlayer: reset() onNext"
        }
        .doOnCompleted {
            Debug.d "RxExoPlayer: reset() onCompleted"
        }
        .doOnSubscribe {
            Debug.d "RxExoPlayer: reset() onSubscribe"
            innerPlayer.stop()
            innerPlayer.seekTo(0)
            innerPlayer.playWhenReady = false
        }
    }
}
