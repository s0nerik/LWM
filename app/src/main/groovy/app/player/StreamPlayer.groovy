package app.player
import android.content.Context
import android.net.Uri
import android.os.Handler
import app.events.client.SendReadyEvent
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

    StreamPlayer() {
        super()
        playbackUri = Uri.parse "http://192.168.49.1:8888/stream"
    }

    void prepare() {
        prepare playbackUri, true
    }

    @Override
    void onReady(boolean playWhenReady) {
//        super.onReady(playWhenReady)
        bus.post new SendReadyEvent()
    }

    @Override
    void nextSong() {

    }

    @Override
    void prevSong() {

    }

    @Override
    void togglePause() {
        if (innerPlayer.playWhenReady) {
            pause()
        } else {
            unpause()
        }
    }

    @Override
    void startService() {

    }

    @Override
    protected String getExtension(String fileName) {
        return "mp3"
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