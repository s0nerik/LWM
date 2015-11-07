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
import rx.Subscriber
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject

import static com.google.android.exoplayer.ExoPlayer.*

@CompileStatic
abstract class RxExoPlayer {

    protected ExoPlayer innerPlayer

    protected SerializedSubject<Boolean, Boolean> prepareSubject = PublishSubject.create().toSerialized()
    protected SerializedSubject<PlaybackEvent, PlaybackEvent> playbackSubject = PublishSubject.create().toSerialized()

    protected int lastState = STATE_IDLE

    enum PlaybackEvent {
        STARTED, PAUSED, ENDED, STOPPED
    }

    private Listener listener = [
            onPlayerStateChanged    : { boolean playWhenReady, int playbackState ->
                switch (playbackState) {
                    case STATE_ENDED:
                        playbackSubject.onNext PlaybackEvent.ENDED
                        break
                    case STATE_IDLE:
                        playbackSubject.onNext PlaybackEvent.STOPPED
                        break
                    case STATE_PREPARING:
                        break
                    case STATE_BUFFERING:
                        break
                    case STATE_READY:
                        prepareSubject.onNext playWhenReady
                        break
                }
                lastState = playbackState

                Debug.d Utils.getConstantName(ExoPlayer, playbackState)
            },
            onPlayWhenReadyCommitted: {

            },
            onPlayerError           : { ExoPlaybackException e ->
                if (lastState == STATE_PREPARING || lastState == STATE_BUFFERING) {
                    prepareSubject.onError e
                }

                if (lastState == STATE_READY) {
                    playbackSubject.onError e
                }
            }
    ] as Listener

    RxExoPlayer() {
        innerPlayer = Factory.newInstance 1, 10000, 20000
        innerPlayer.addListener listener
    }

    // region Player state
    boolean isPaused() { !innerPlayer.playWhenReady }

    boolean isPlaying() { ready && !paused }

    boolean isReady() { innerPlayer.playbackState == STATE_READY }

    int getCurrentPosition() { innerPlayer.currentPosition }
    // endregion

    protected abstract TrackRenderer getRenderer(Uri uri)

    Observable<Boolean> start() {
        Observable.create({ Subscriber<Boolean> subscriber ->
            innerPlayer.playWhenReady = true
            prepareSubject.first().subscribe subscriber
        } as Observable.OnSubscribe<Boolean>)
    }

    Observable<Boolean> pause() {
        Observable.create({ Subscriber<Boolean> subscriber ->
            innerPlayer.playWhenReady = false
            prepareSubject.first().subscribe subscriber
        } as Observable.OnSubscribe<Boolean>)
    }

    Observable<Boolean> setPaused(boolean flag) {
        if (flag)
            pause()
        else
            start()
    }

    Observable<Boolean> togglePause() { setPaused !paused }

    Observable<PlaybackEvent> stop() {
        Observable.create({ Subscriber<PlaybackEvent> subscriber ->
            innerPlayer.stop()
            playbackSubject.filter({it == PlaybackEvent.STOPPED}).first().subscribe subscriber
        } as Observable.OnSubscribe<PlaybackEvent>)
    }

    Observable<Boolean> prepare(@NonNull Uri uri) {
        Observable.create({ Subscriber<Boolean> subscriber ->
            try {
                innerPlayer.prepare getRenderer(uri)
                prepareSubject.first().subscribe subscriber
            } catch (e) {
                subscriber.onError(e)
            }
        } as Observable.OnSubscribe<Boolean>)
    }

    Observable<Boolean> seekTo(int msec) {
        Observable.create({ Subscriber<Boolean> subscriber ->
            try {
                innerPlayer.seekTo msec
                prepareSubject.first().subscribe subscriber
            } catch (e) {
                subscriber.onError(e)
            }
        } as Observable.OnSubscribe<Boolean>)
    }
}
