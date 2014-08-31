package com.lwm.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.view.SurfaceHolder;

import com.lwm.app.model.Song;
import com.lwm.app.player.StreamPlayer;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

/**
 *
 * Created by sonerik on 7/19/14.
 */
public class StreamPlayerService extends Service {

    private final StreamPlayerServiceBinder binder = new StreamPlayerServiceBinder();
    private StreamPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new StreamPlayer(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
        player = null;
    }

    public void setPlayer(StreamPlayer player) {
        if (player != null) {
            player.release();
        }
        this.player = player;
    }

    public StreamPlayer getPlayer() {
        return player;
    }

    public void attachToStation() {
        player.register();
    }

    public void start() throws IllegalStateException {
        player.start();
    }

    public void nextSong() {
        player.nextSong();
    }

    public void prevSong() {
        player.prevSong();
    }

    public void togglePause() {
        player.togglePause();
    }

    public void pause() throws IllegalStateException {
        player.pause();
    }

    public void prepareNewSong() {
        player.prepareNewSong();
    }

    public Song getCurrentSong() {
        return player.getCurrentSong();
    }

    public void setCurrentSong(Song currentSong) {
        player.setCurrentSong(currentSong);
    }

    public void stop() throws IllegalStateException {
        player.stop();
    }

    public void detachFromStation() {
        player.unregister();
    }

    public static boolean isActive() {
        return StreamPlayer.isActive();
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

    public File getTempFile() {
        return player.getTempFile();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class StreamPlayerServiceBinder extends Binder {
        public StreamPlayerService getService() {
            return StreamPlayerService.this;
        }
    }

}
