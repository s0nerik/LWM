package app.player
import android.content.Context
import android.net.Uri
import android.os.Handler
import app.events.player.ReadyToStartPlaybackEvent
import app.model.Song
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
class StreamPlayer extends BasePlayer {

    @Inject
    @PackageScope
    Context context

    @Inject
    @PackageScope
    Bus bus

    @Inject
    @PackageScope
    Handler handler

    private Song currentSong

    private int positionToPrepare = -1
    private boolean seekingToPosition = false

    @Override
    void onReady(boolean playWhenReady) {
        super.onReady(playWhenReady)

        if (positionToPrepare >= 0) {
            def pos = positionToPrepare
            positionToPrepare = -1

            seekingToPosition = true
            seekTo pos
        } else if (seekingToPosition && paused) {
            seekingToPosition = false
            bus.post new ReadyToStartPlaybackEvent(this, currentSong, currentPosition as int)
        }
    }

    void prepareForPosition(int pos) {
        positionToPrepare = pos
        prepare(currentSong)
    }

    @Override
    void startService() {

    }

    void setCurrentSong (Song s) {
        currentSong = s
    }

}