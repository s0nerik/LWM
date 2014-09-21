package com.lwm.app.player;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.lwm.app.App;
import com.lwm.app.event.player.PlaybackPausedEvent;
import com.lwm.app.event.player.PlaybackStartedEvent;
import com.lwm.app.event.player.PlaylistAddedToQueueEvent;
import com.lwm.app.event.player.QueueShuffledEvent;
import com.lwm.app.event.player.SongAddedToQueueEvent;
import com.lwm.app.model.Song;
import com.lwm.app.server.ClientsStateListener;
import com.lwm.app.server.StreamServer;
import com.lwm.app.server.async.ClientsManager;
import com.lwm.app.ui.notification.NowPlayingNotification;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class LocalPlayer extends BasePlayer implements ClientsStateListener {

    private Context context;

    private boolean repeat = false;

    private boolean active = false;

    private Queue queue;

    private AudioManager audioManager;
    private NotificationManager notificationManager;

    private ClientsManager clientsManager;

    private OnCompletionListener onCompletionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Log.d("LWM", "LocalPlayer: onCompletion");

            if(getCurrentPosition() > getDuration()-1000){
                if(isRepeat()){
                    play();
                }else{
                    nextSong();
                }
            }
        }
    };

    private OnSeekCompleteListener onSeekCompleteListener = new OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            start();
        }
    };

    public LocalPlayer(Context context){
        this.context = context;

        clientsManager = new ClientsManager(context, this);

        setOnCompletionListener(onCompletionListener);
        setOnSeekCompleteListener(onSeekCompleteListener);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public List<Song> getQueue(){
        return queue.getQueue();
    }

    public void setQueue(List<Song> songs){
        queue = new Queue(this, songs);
    }

    public void shuffleQueue(){
        queue.shuffle();
        App.getEventBus().post(new QueueShuffledEvent(queue.getQueue()));
    }

    public void shuffleQueueExceptPlayed(){
        queue.shuffleExceptPlayed();
        App.getEventBus().post(new QueueShuffledEvent(queue.getQueue()));
    }

    public void addToQueue(Collection<Song> songs){
        queue.addSongs(songs);
        App.getEventBus().post(new PlaylistAddedToQueueEvent(queue.getQueue()));
    }

    public void addToQueue(Song song){
        queue.addSong(song);
        App.getEventBus().post(new SongAddedToQueueEvent(queue.getQueue(), song));
    }

    public Song getCurrentSong(){
        Log.d(App.TAG, "getCurrentSong");
        return queue.getSong();
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
    }

    public void play(int position){
        queue.moveTo(position);
        play();
    }

    public void play(){
        reset();
        try {
            assert queue != null : "queue == null";
            setDataSource(queue.getSong().getSource());
            prepare();

            active = true;

            if(StreamServer.hasClients()) {
                clientsManager.changeSong();
            }else{
                start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasCurrentSong() {
        return active;
    }

    @Override
    public void nextSong() {
        Log.d(App.TAG, "LocalPlayer: nextSong");

        if(queue.moveToNext()) {
            play();
        } else {
            Toast.makeText(context, "There's no next song", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        if(StreamServer.hasClients()) {
            clientsManager.seekTo(msec);
        }
        super.seekTo(msec);
    }

    public boolean isShuffle() {
        return queue.isShuffled();
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean flag) {
        repeat = flag;
    }

    @Override
    public void prevSong(){
        Log.d(App.TAG, "LocalPlayer: prevSong");

        if(queue.moveToPrev()) {
            play();
        } else {
            Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();

        updateNotificationIfForeground();

        App.getEventBus().post(new PlaybackPausedEvent(queue.getSong(), getCurrentPosition()));

        context.sendOrderedBroadcast(new Intent(NowPlayingNotification.ACTION_SHOW), null);

    }

    @Override
    public void start() throws IllegalStateException {
        super.start();

        updateNotificationIfForeground();

        App.getEventBus().post(new PlaybackStartedEvent(queue.getSong(), getCurrentPosition()));

        context.sendOrderedBroadcast(new Intent(NowPlayingNotification.ACTION_SHOW), null);

        audioManager.requestAudioFocus(afListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

    }

    @Override
    public void togglePause(){
        if (isPlaying()){
            if(StreamServer.hasClients()){
                clientsManager.pause();
            }
            pause();
        } else {
            if(StreamServer.hasClients()){
                clientsManager.unpause(getCurrentPosition());
            }
            start();
        }
    }

    private void updateNotificationIfForeground() {
        if (App.isLocalPlayerServiceInForeground())
            notificationManager.notify(
                    NowPlayingNotification.NOTIFICATION_ID,
                    NowPlayingNotification.create(context));
    }

    public int getCurrentQueuePosition() {
        return queue.getCurrentIndex();
    }

    @Override
    public void onClientsReady() {
        start();
    }

    @Override
    public void onWaitClients() {}

    public int getQueueSize() {
        return queue.getSize();
    }

    private AFListener afListener = new AFListener();

    private class AFListener implements AudioManager.OnAudioFocusChangeListener {

        @Override
        public void onAudioFocusChange(int focusChange) {
            String event = "";
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    event = "AUDIOFOCUS_LOSS";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    event = "AUDIOFOCUS_LOSS_TRANSIENT";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    event = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    event = "AUDIOFOCUS_GAIN";
                    break;
            }
            Log.d(App.TAG, "onAudioFocusChange: " + event);
        }
    }
}