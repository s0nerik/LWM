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
        Debug.d "prepareForPosition: ${pos}"

        reset().onErrorResumeNext(Observable.empty())
               .doOnSubscribe { currentSong = song }
               .concatWith(prepare())
               .concatWith(seekTo(pos))
    }

    @Override
    void startService() {
//        context.startService new Intent(context, StreamPlayerService)
    }

}