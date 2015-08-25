package app.player
import android.content.Context
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

    @Override
    void onReady(boolean playWhenReady) {
        if (!playing)
            bus.post new SendReadyEvent()
    }

    @Override
    void nextSong() {

    }

    @Override
    void prevSong() {

    }

    @Override
    void startService() {

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