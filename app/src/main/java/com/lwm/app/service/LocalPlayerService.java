package com.lwm.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.view.SurfaceHolder;

import com.lwm.app.App;
import com.lwm.app.events.server.AllClientsReadyEvent;
import com.lwm.app.events.server.PauseClientsEvent;
import com.lwm.app.events.server.StartClientsEvent;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.squareup.otto.Subscribe;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 *
 * Created by sonerik on 7/19/14.
 */
public class LocalPlayerService extends Service {

    private final LocalPlayerServiceBinder binder = new LocalPlayerServiceBinder();
    private LocalPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new LocalPlayer(this);
        App.getBus().register(this);
    }

    @Override
    public void onDestroy() {
        player.release();
        player = null;
        App.getBus().unregister(this);
        super.onDestroy();
    }

    public void setPlayer(LocalPlayer player) {
        if (player != null) {
            player.release();
        }
        this.player = player;
    }

    public LocalPlayer getPlayer() {
        return player;
    }

    public List<Song> getQueue() {
        return player.getQueue();
    }

    public void setQueue(List<Song> queue) {
        player.setQueue(queue);
    }

    public void shuffleQueue() {
        player.shuffleQueue();
    }

    public void shuffleQueueExceptPlayed() {
        player.shuffleQueueExceptPlayed();
    }

    public void addToQueue(Collection<Song> songs) {
        player.addToQueue(songs);
    }

    public void addToQueue(Song song) {
        player.addToQueue(song);
    }

    public Song getCurrentSong() {
        return player.getCurrentSong();
    }

    public void stop() throws IllegalStateException {
        player.stop();
    }

    public void play(int position) {
        player.play(position);
    }

    public boolean hasCurrentSong() {
        return player.hasCurrentSong();
    }

    public void nextSong() {
        player.nextSong();
    }

    public boolean isShuffle() {
        return player.isShuffle();
    }

    public boolean isRepeat() {
        return player.isRepeat();
    }

    public void setRepeat(boolean flag) {
        player.setRepeat(flag);
    }

    public void prevSong() {
        player.prevSong();
    }

    public void pause() throws IllegalStateException {
        player.pause();
    }

    public void start() throws IllegalStateException {
        player.start();
    }

    public void togglePause() {
        player.togglePause();
    }

    public int getCurrentQueuePosition() {
        return player.getCurrentQueuePosition();
    }

    public int getQueueSize() {
        return player.getQueueSize();
    }

    public String getCurrentDurationInMinutes() {
        return player.getCurrentDurationInMinutes();
    }

    public String getCurrentPositionInMinutes() {
        return player.getCurrentPositionInMinutes();
    }

    public void setDisplay(SurfaceHolder sh) {
        player.setDisplay(sh);
    }

    public static MediaPlayer create(Context context, Uri uri) {
        return MediaPlayer.create(context, uri);
    }

    public static MediaPlayer create(Context context, Uri uri, SurfaceHolder holder) {
        return MediaPlayer.create(context, uri, holder);
    }

    public static MediaPlayer create(Context context, int resid) {
        return MediaPlayer.create(context, resid);
    }

    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        player.setDataSource(context, uri);
    }

    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        player.setDataSource(path);
    }

    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        player.setDataSource(fd);
    }

    public void setDataSource(FileDescriptor fd, long offset, long length) throws IOException, IllegalArgumentException, IllegalStateException {
        player.setDataSource(fd, offset, length);
    }

    public void prepare() throws IOException, IllegalStateException {
        player.prepare();
    }

    public void prepareAsync() throws IllegalStateException {
        player.prepareAsync();
    }

    public void setWakeMode(Context context, int mode) {
        player.setWakeMode(context, mode);
    }

    public void setScreenOnWhilePlaying(boolean screenOn) {
        player.setScreenOnWhilePlaying(screenOn);
    }

    public int getVideoWidth() {
        return player.getVideoWidth();
    }

    public int getVideoHeight() {
        return player.getVideoHeight();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void seekTo(int i) throws IllegalStateException {
        player.seekTo(i);
    }

    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public int getDuration() {
        return player.getDuration();
    }

    public void release() {
        player.release();
    }

    public void reset() {
        player.reset();
    }

    public void setAudioStreamType(int i) {
        player.setAudioStreamType(i);
    }

    public void setLooping(boolean b) {
        player.setLooping(b);
    }

    public boolean isLooping() {
        return player.isLooping();
    }

    public void setVolume(float v, float v2) {
        player.setVolume(v, v2);
    }

    public void setAudioSessionId(int i) throws IllegalArgumentException, IllegalStateException {
        player.setAudioSessionId(i);
    }

    public int getAudioSessionId() {
        return player.getAudioSessionId();
    }

    public void attachAuxEffect(int i) {
        player.attachAuxEffect(i);
    }

    public void setAuxEffectSendLevel(float v) {
        player.setAuxEffectSendLevel(v);
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
        player.setOnPreparedListener(listener);
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        player.setOnCompletionListener(listener);
    }

    public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener listener) {
        player.setOnBufferingUpdateListener(listener);
    }

    public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener listener) {
        player.setOnSeekCompleteListener(listener);
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
        player.setOnErrorListener(listener);
    }

    public void setOnInfoListener(MediaPlayer.OnInfoListener listener) {
        player.setOnInfoListener(listener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalPlayerServiceBinder extends Binder {
        public LocalPlayerService getService() {
            return LocalPlayerService.this;
        }
    }

    @Subscribe
    public void allClientsReady(AllClientsReadyEvent event) {
        start();
    }

    @Subscribe
    public void onStartClients(StartClientsEvent event) {
        start();
    }

    @Subscribe
    public void onPauseClients(PauseClientsEvent event) {
        pause();
    }


}
