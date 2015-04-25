package com.lwm.app.ui.fragment.playback;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;
import com.lwm.app.R;
import com.lwm.app.events.player.RepeatStateChangedEvent;
import com.lwm.app.events.player.playback.PlaybackPausedEvent;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.player.playback.SongChangedEvent;
import com.lwm.app.events.player.playback.SongPlayingEvent;
import com.lwm.app.events.player.queue.QueueShuffledEvent;
import com.lwm.app.model.Song;
import com.lwm.app.player.BasePlayer;
import com.lwm.app.player.StreamPlayer;
import com.lwm.app.server.StreamServer;
import com.lwm.app.ui.Blur;
import com.squareup.otto.Subscribe;

import java.util.UUID;

import javax.inject.Inject;

import ru.noties.debug.Debug;

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
