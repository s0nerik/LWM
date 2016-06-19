package app.players
import android.os.Handler
import app.models.Song
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug
import rx.Observable

import javax.inject.Inject

@CompileStatic
class StreamPlayer extends BasePlayer {

    @Inject
    @PackageScope
    Handler handler

    Observable prepareForPosition(Song song, int pos) {
        reset().doOnSubscribe { Debug.d "prepareForPosition: ${pos}" }
               .onErrorResumeNext(Observable.just(PlayerEvent.IDLE))
               .concatMap { prepare(song) }
               .concatMap { seekTo(pos) }
    }

    @Override
    void startService() {
//        context.startService new Intent(context, StreamPlayerService)
    }

    @Override
    protected int getBufferSegmentSize() { 16 * 1024 }

    @Override
    protected int getBufferSegmentCount() { 256 }
}