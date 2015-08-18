package app.player
import android.content.Context
import android.os.Handler
import app.Injector
import app.server.StreamServer
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import ru.noties.debug.Debug

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class StreamPlayer extends BasePlayer {

    @Inject
    @PackageScope
    Context context

    @Inject
    @PackageScope
    Bus bus

    @Inject
    @PackageScope
    Handler handler

    static boolean active = false

    public static final String STREAM_PATH = StreamServer.Url.STREAM;

    public StreamPlayer() {
        super()
        Injector.inject this
//        onSeekCompleteListener = { MediaPlayer mediaPlayer ->
//            Debug.d("StreamPlayer: onSeekComplete");
//            start();
//        }
//        onPreparedListener = { MediaPlayer mediaPlayer ->
//            Debug.d("StreamPlayer: onPrepared");
//            bus.post(new SendReadyEvent());
//        }
//        onBufferingUpdateListener = { MediaPlayer mp, int percent ->
//            Debug.d("Buffered: "+percent);
//        }
    }

    public void prepareNewSong(){
//        reset();
//        try {
//            setDataSource(STREAM_PATH);
//            prepareAsync();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void nextSong() {
        Debug.d()
        prepareNewSong()
    }

    @Override
    public void prevSong() {
        Debug.d()
        prepareNewSong()
    }

    @Override
    public void togglePause() {
        if (innerPlayer.playWhenReady) {
            pause()
        } else {
            unpause()
        }
    }

    @Override
    void startService() {

    }

//    @Override
//    public void start() throws IllegalStateException {
//        super.start();
//        updateSongInfo();
//    }

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