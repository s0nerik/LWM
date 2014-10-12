package com.lwm.app.player;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.lwm.app.App;
import com.lwm.app.events.player.PlaybackPausedEvent;
import com.lwm.app.events.player.PlaybackStartedEvent;
import com.lwm.app.events.player.PlaylistAddedToQueueEvent;
import com.lwm.app.events.player.QueueShuffledEvent;
import com.lwm.app.events.player.SongAddedToQueueEvent;
import com.lwm.app.events.server.PauseClientsEvent;
import com.lwm.app.events.server.PrepareClientsEvent;
import com.lwm.app.events.server.SeekToClientsEvent;
import com.lwm.app.events.server.StartClientsEvent;
import com.lwm.app.model.Song;
import com.lwm.app.ui.notification.NowPlayingNotification;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class LocalPlayer extends BasePlayer {

    private Context context;

    private boolean repeat = false;

    private boolean active = false;

    private Queue queue = new Queue();

    private NotificationManager notificationManager;

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
        super(context);
        this.context = context;

        setOnCompletionListener(onCompletionListener);
        setOnSeekCompleteListener(onSeekCompleteListener);

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public List<Song> getQueue(){
        return queue.getQueue();
    }

    public void setQueue(List<Song> songs){
        queue = new Queue(songs);
    }

    public void shuffleQueue(){
        queue.shuffle();
        App.getBus().post(new QueueShuffledEvent(queue.getQueue()));
    }

    public void shuffleQueueExceptPlayed(){
        queue.shuffleExceptPlayed();
        App.getBus().post(new QueueShuffledEvent(queue.getQueue()));
    }

    public void addToQueue(Collection<Song> songs){
        queue.addSongs(songs);
        App.getBus().post(new PlaylistAddedToQueueEvent(queue.getQueue()));
    }

    public void addToQueue(Song song){
        queue.addSong(song);
        App.getBus().post(new SongAddedToQueueEvent(queue.getQueue(), song));
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

            if (App.isServerStarted()) {
                App.getBus().post(new PrepareClientsEvent());
            } else {
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
        Log.d(App.TAG, "LocalPlayer: seekTo("+msec+")");
        if (App.isServerStarted()) {
            App.getBus().post(new SeekToClientsEvent(msec));
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

        App.getBus().post(new PlaybackPausedEvent(queue.getSong(), getCurrentPosition()));

        context.sendOrderedBroadcast(new Intent(NowPlayingNotification.ACTION_SHOW), null);
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();

        updateNotificationIfForeground();

        App.getBus().post(new PlaybackStartedEvent(queue.getSong(), getCurrentPosition()));

        context.sendOrderedBroadcast(new Intent(NowPlayingNotification.ACTION_SHOW), null);
    }

    @Override
    public void togglePause(){
        if (isPlaying()){
            if(App.isServerStarted()) {
                App.getBus().post(new PauseClientsEvent());
            } else {
                pause();
            }
        } else {
            if(App.isServerStarted()) {
                App.getBus().post(new StartClientsEvent());
            } else {
                start();
            }
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

    public int getQueueSize() {
        return queue.getSize();
    }

}