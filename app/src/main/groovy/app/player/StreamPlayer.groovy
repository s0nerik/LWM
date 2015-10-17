package app.player

import android.os.Handler
import app.model.Song
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
class StreamPlayer extends BasePlayer {

    @Inject
    @PackageScope
    Handler handler

    Song song

    private int positionToPrepare = -1
    private boolean seekingToPosition = false

//    @Override
//    void onReady(boolean playWhenReady) {
//        super.onReady(playWhenReady)
//
//        if (positionToPrepare >= 0) {
//            def pos = positionToPrepare
//            positionToPrepare = -1
//
//            seekingToPosition = true
//            seekTo pos
//        } else if (seekingToPosition && paused) {
//            seekingToPosition = false
//            bus.post new ReadyToStartPlaybackEvent(this, currentSong, currentPosition as int)
//        }
//    }

    void prepareForPosition(int pos) {
        positionToPrepare = pos
        prepare(song)
    }

    @Override
    void startService() {

    }

}