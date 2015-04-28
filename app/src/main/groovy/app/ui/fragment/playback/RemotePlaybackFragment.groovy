package app.ui.fragment.playback
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
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
import app.ui.Blur
import com.koushikdutta.ion.Ion
import com.koushikdutta.ion.bitmap.Transform
import app.R
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import ru.noties.debug.Debug

import javax.inject.Inject

@CompileStatic
public class RemotePlaybackFragment extends PlaybackFragment {

    @Inject
    StreamPlayer player;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mControls.setVisibility(View.GONE);
        mSeekBar.setEnabled(false);
        mSeekBar.setBackgroundResource(R.drawable.background_seekbar_no_controls);
    }

    @Override
    protected BasePlayer getPlayer() {
        return player;
    }

    @Override
    protected void setCover(Song song) {
        Ion.with(mCover)
                .crossfade(true)
                .placeholder(R.drawable.no_cover)
                .error(R.drawable.no_cover)
                .smartSize(true)
                .load(StreamServer.Url.CURRENT_ALBUMART + "?" + UUID.randomUUID());
    }

    @Override
    protected void setBackground(final Song song) {
        Ion.with(mBackground)
                .placeholder(R.drawable.no_cover_blurred)
                .error(R.drawable.no_cover_blurred)
                .crossfade(true)
                .smartSize(true)
                .transform(new Transform() {
                    @Override
                    public Bitmap transform(Bitmap b) {
                        return new Blur().blur(b);
                    }

                    @Override
                    public String key() {
                        return song.getTitle();
                    }
                })
                .load(StreamServer.Url.CURRENT_ALBUMART);
    }

    @Subscribe
    @Override
    public void onSongChanged(SongChangedEvent event) {
        Debug.d("RemotePlaybackFragment: onSongChanged");
    }

    @Subscribe
    @Override
    public void onPlaybackStarted(PlaybackStartedEvent event) {
        setSongInfo(event.getSong());
        Debug.d("RemotePlaybackFragment: onPlaybackStarted");
    }

    @Subscribe
    @Override
    public void onPlaybackPaused(PlaybackPausedEvent event) {
        Debug.d("RemotePlaybackFragment: onPlaybackPaused");
    }

    @Subscribe
    @Override
    public void onSongPlaying(SongPlayingEvent event) {
        super.onSongPlaying(event);
    }

    @Subscribe
    @Override
    public void onQueueShuffled(QueueShuffledEvent event) {
        Debug.d("RemotePlaybackFragment: onQueueShuffled");
    }

    @Subscribe
    @Override
    public void onRepeatStateChanged(RepeatStateChangedEvent event) {
        Debug.d("RemotePlaybackFragment: onRepeatStateChanged");
    }

}
