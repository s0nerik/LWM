package app.ui.fragment.playback
import android.os.Bundle
import android.view.View
import app.R
import app.events.player.RepeatStateChangedEvent
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongChangedEvent
import app.events.player.playback.SongPlayingEvent
import app.events.player.queue.QueueShuffledEvent
import app.model.Song
import app.player.BasePlayer
import app.player.StreamPlayer
import app.server.StreamServer
import com.bumptech.glide.Glide
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug

import javax.inject.Inject

@CompileStatic
class RemotePlaybackFragment extends PlaybackFragment {

    @Inject
    @PackageScope
    StreamPlayer player

    @Override
    void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)
        controls.visibility = View.GONE
        seekBar.enabled = false
        seekBar.backgroundResource = R.drawable.background_seekbar_no_controls
    }

    @Override
    protected BasePlayer getPlayer() { player }

    @Override
    protected void setSongInfo(Song song) {
        super.setSongInfo(song)

        // Load song cover into cover view
        Glide.with(this)
                .load("${StreamServer.Url.CURRENT_ALBUMART}?${UUID.randomUUID()}")
                .centerCrop()
                .crossFade()
                .placeholder(R.drawable.no_cover)
                .error(R.drawable.no_cover)
                .into(cover)

        // Load blurred song cover into background view
        Glide.with(this)
                .load(StreamServer.Url.CURRENT_ALBUMART)
                .placeholder(R.drawable.no_cover_blurred)
                .error(R.drawable.no_cover_blurred)
                .crossFade()
                .centerCrop()
                .into(background)
//                .transform(new Transform() {
//                    @Override
//                    public Bitmap transform(Bitmap b) {
//                        return new Blur().blur(b);
//                    }
//
//                    @Override
//                    public String key() {
//                        return song.getTitle();
//                    }
//                })
    }

    @Subscribe
    @Override
    void onSongChanged(SongChangedEvent event) {
        Debug.d "RemotePlaybackFragment: onSongChanged"
    }

    @Subscribe
    @Override
    void onPlaybackStarted(PlaybackStartedEvent event) {
        songInfo = event.song
        Debug.d "RemotePlaybackFragment: onPlaybackStarted"
    }

    @Subscribe
    @Override
    void onPlaybackPaused(PlaybackPausedEvent event) {
        Debug.d "RemotePlaybackFragment: onPlaybackPaused"
    }

    @Subscribe
    @Override
    void onSongPlaying(SongPlayingEvent event) {
        super.onSongPlaying(event)
    }

    @Subscribe
    @Override
    void onQueueShuffled(QueueShuffledEvent event) {
        Debug.d "RemotePlaybackFragment: onQueueShuffled"
    }

    @Subscribe
    @Override
    void onRepeatStateChanged(RepeatStateChangedEvent event) {
        Debug.d "RemotePlaybackFragment: onRepeatStateChanged"
    }

}
