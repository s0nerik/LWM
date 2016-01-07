package app.player
import android.os.Handler
import app.model.Song
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

    Song song

    Song currentSong

    Observable prepareForPosition(int pos) {
        reset().doOnSubscribe { Debug.d "prepareForPosition: ${pos}" }
               .onErrorResumeNext(Observable.empty())
               .doOnSubscribe { currentSong = song }
               .concatWith(prepare())
               .concatWith(seekTo(pos))
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