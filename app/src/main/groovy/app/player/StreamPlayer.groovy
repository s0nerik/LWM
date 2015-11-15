package app.player

import android.os.Handler
import app.model.Song
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import rx.Observable

import javax.inject.Inject

@CompileStatic
class StreamPlayer extends BasePlayer {

    @Inject
    @PackageScope
    Handler handler

    Song song

    private int positionToPrepare = -1

    Song currentSong

    Observable prepareForPosition(int pos) {
        positionToPrepare = pos
        currentSong = song
        prepare().concatWith(seekTo(pos))
    }

    @Override
    void startService() {

    }

}