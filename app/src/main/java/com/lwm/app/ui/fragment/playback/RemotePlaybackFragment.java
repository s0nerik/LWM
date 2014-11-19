package com.lwm.app.ui.fragment.playback;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lwm.app.App;
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
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

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
    protected String getCoverUrl(Song song) {
        return StreamServer.Url.CURRENT_ALBUMART;
    }

    @Subscribe
    @Override
    public void onSongChanged(SongChangedEvent event) {
        Log.d(App.TAG, "RemotePlaybackFragment: onSongChanged");
    }

    @Subscribe
    @Override
    public void onPlaybackStarted(PlaybackStartedEvent event) {
        Log.d(App.TAG, "RemotePlaybackFragment: onPlaybackStarted");
    }

    @Subscribe
    @Override
    public void onPlaybackPaused(PlaybackPausedEvent event) {

        Log.d(App.TAG, "RemotePlaybackFragment: onPlaybackPaused");
    }

    @Subscribe
    @Override
    public void onSongPlaying(SongPlayingEvent event) {
        Log.d(App.TAG, "RemotePlaybackFragment: onSongPlaying");
    }

    @Subscribe
    @Override
    public void onQueueShuffled(QueueShuffledEvent event) {
        Log.d(App.TAG, "RemotePlaybackFragment: onQueueShuffled");
    }

    @Subscribe
    @Override
    public void onRepeatStateChanged(RepeatStateChangedEvent event) {
        Log.d(App.TAG, "RemotePlaybackFragment: onRepeatStateChanged");
    }

}
