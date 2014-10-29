package com.lwm.app.ui.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.model.Song;
import com.lwm.app.player.BasePlayer;
import com.lwm.app.task.SeekBarUpdateTask;
import com.lwm.app.ui.Croutons;
import com.lwm.app.ui.base.DaggerActivity;
import com.lwm.app.ui.fragment.PlaybackFragment;
import com.lwm.app.websocket.entities.ClientInfo;
import com.squareup.otto.Bus;

import java.util.Timer;

import javax.inject.Inject;

public abstract class PlaybackActivity extends DaggerActivity {

    protected PlaybackFragment playbackFragment;
    protected MediaPlayer player;
    protected ActionBar actionBar;

    protected MenuItem broadcastButton;

    protected Utils utils;

    protected long currentAlbumId;

    protected Timer seekBarUpdateTimer = new Timer();

    public abstract void onControlButtonClicked(View v);

    protected abstract void setSongInfo(Song song);

    @Inject
    Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utils = new Utils(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        initActionBar();

        playbackFragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelSeekBarUpdater();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onClientConnected(ClientInfo info) {
        Croutons.clientConnected(this, info, R.id.offsetted_albumart).show();
    }

    protected void onClientDisconnected(ClientInfo info) {
        Croutons.clientDisconnected(this, info, R.id.offsetted_albumart).show();
    }

    protected void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_playback_activity);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        actionBar.setCustomView(inflater.inflate(R.layout.actionbar_listen, null));
    }

    protected void initSeekBarUpdater(BasePlayer player) {
        seekBarUpdateTimer.cancel();
        seekBarUpdateTimer.purge();
        seekBarUpdateTimer = new Timer();
        seekBarUpdateTimer.schedule(new SeekBarUpdateTask(playbackFragment, player, player.getDuration()), 0, PlaybackFragment.SEEK_BAR_UPDATE_INTERVAL);
    }

    protected void initSeekBarUpdater(BasePlayer player, int duration) {
        seekBarUpdateTimer.cancel();
        seekBarUpdateTimer.purge();
        seekBarUpdateTimer = new Timer();
        seekBarUpdateTimer.schedule(new SeekBarUpdateTask(playbackFragment, player, duration), 0, PlaybackFragment.SEEK_BAR_UPDATE_INTERVAL);
    }

    protected void cancelSeekBarUpdater() {
        seekBarUpdateTimer.cancel();
        seekBarUpdateTimer.purge();
    }

}