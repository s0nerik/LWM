package app.player
import android.content.Context
import android.net.Uri
import android.os.Handler
import app.events.client.ReadyToStartPlaybackEvent
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

    private int positionToPrepare = -1
    private boolean seekingToPosition = false

    @Override
    void onReady(boolean playWhenReady) {
        if (positionToPrepare >= 0) {
            def pos = positionToPrepare
            positionToPrepare = -1

            seekingToPosition = true
            seekTo pos
        } else if (seekingToPosition && paused) {
            seekingToPosition = false
            bus.post new ReadyToStartPlaybackEvent()
        }
    }

    void prepareForPosition(Uri uri, int pos) {
        positionToPrepare = pos
        prepare uri, true
    }

//    @Override
//    void seekTo(int msec) {
//        paused = true
//    }

    @Override
    void nextSong() {

    }

    @Override
    void prevSong() {

    }

    @Override
    void startService() {

    }

    @Override
    Song getCurrentSong() {
        return new Song()
    }

    private void updateSongInfo() {
        // TODO: make it work
//        Ion.with(context)
//                .load(StreamServer.Url.CURRENT_INFO)
//                .as(Song.class)
//                .withResponse()
//                .setCallback(new FutureCallback<Response<Song>>() {
//                    @Override
//                    public void onCompleted(Exception e, Response<Song> result) {
//                        if (e == null) {
//                            setCurrentSong(result.getResult());
//                            bus.post(new PlaybackStartedEvent(result.getResult(), getCurrentPosition()));
//                            startNotifyingPlaybackProgress();
//                        } else {
//                            Debug.e("Error getting song info", e);
//                        }
//                    }
//                });
    }
}