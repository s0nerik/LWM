package com.lwm.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.helper.SongsCursorGetter;
import com.lwm.app.model.MusicPlayer;
import com.lwm.app.model.StreamPlayer;

public class MusicService extends Service {

    public static final String ACTION_PLAY_SONG = "com.lwm.player.action.PLAY_SONG";
    public static final String ACTION_PAUSE_SONG = "com.lwm.player.action.PAUSE_SONG";
    public static final String ACTION_UNPAUSE_SONG = "com.lwm.player.action.UNPAUSE_SONG";
    public static final String ACTION_NEXT_SONG = "com.lwm.player.action.NEXT_SONG";
    public static final String ACTION_PREV_SONG = "com.lwm.player.action.PREV_SONG";
    public static final String ACTION_SONG_SEEK_TO = "com.lwm.player.action.SONG_SEEK_TO";
    public static final String ACTION_SHUFFLE_ON = "com.lwm.player.action.SHUFFLE_ON";
    public static final String ACTION_SHUFFLE_OFF = "com.lwm.player.action.SHUFFLE_OFF";
    public static final String ACTION_REPEAT_ON = "com.lwm.player.action.REPEAT_ON";
    public static final String ACTION_REPEAT_OFF = "com.lwm.player.action.REPEAT_OFF";

    public static final String ACTION_PLAY_STREAM = "com.lwm.player.action.PLAY_STREAM";
    public static final String ACTION_STREAM_NEXT_SONG = "com.lwm.player.action.PLAY_STREAM";
    public static final String ACTION_STREAM_PAUSE = "com.lwm.player.action.STREAM_PAUSE";
    public static final String ACTION_STREAM_UNPAUSE = "com.lwm.player.action.STREAM_UNPAUSE";

    private static MusicPlayer player;
    private static StreamPlayer streamPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(App.TAG, "MusicService.onStartCommand");

        String action = intent.getAction();

        switch(action){
            case ACTION_PLAY_SONG:
                Log.d(App.TAG, "MusicService: ACTION_PLAY_SONG");
                int pos = intent.getIntExtra(MusicPlayer.PLAYLIST_POSITION, -1);
                play(pos);
                sendBroadcast(new Intent(MusicPlayer.PLAYBACK_STARTED));
                break;

            case ACTION_PAUSE_SONG:
                Log.d(App.TAG, "MusicService: ACTION_PAUSE_SONG");
                player.pause();
                sendBroadcast(new Intent(MusicPlayer.PLAYBACK_PAUSED));
                break;

            case ACTION_UNPAUSE_SONG:
                Log.d(App.TAG, "MusicService: ACTION_UNPAUSE_SONG");
                player.start();
                sendBroadcast(new Intent(MusicPlayer.PLAYBACK_STARTED));
                break;

            case ACTION_SONG_SEEK_TO:
                Log.d(App.TAG, "MusicService: ACTION_SONG_SEEK_TO");
                int newPos = intent.getIntExtra(MusicPlayer.SEEK_POSITION, -1);
                Log.d(App.TAG, "MusicService: seekTo("+newPos+")");
                player.seekTo(newPos);
                break;

            case ACTION_NEXT_SONG:
                Log.d(App.TAG, "MusicService: ACTION_NEXT_SONG");
                player.nextSong();
                break;

            case ACTION_PREV_SONG:
                Log.d(App.TAG, "MusicService: ACTION_PREV_SONG");
                player.prevSong();
                break;

            case ACTION_PLAY_STREAM:
                Log.d(App.TAG, "MusicService: ACTION_PLAY_STREAM");
                playStream();
                break;
        }
        return Service.START_STICKY;
    }

    private void play(int pos){
        if(player == null){
            player = new MusicPlayer(this, new SongsCursorGetter(this).getSongs());
        }
        player.play(pos);
    }

    private void playStream() {
        Log.d(App.TAG, "MusicService.playStream()");

        if(streamPlayer == null){
            streamPlayer = new StreamPlayer(this);
        }
        streamPlayer.play();

    }

    private void pause() {
        Log.d(App.TAG, "MusicService.pause()");
        player.pause();
    }

    public static MusicPlayer getCurrentPlayer(){
        return player;
    }

            // TODO: start activity with info about current playing song from stream.

/*        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(fis.getFD());

                currentSong.setSource(dir.getAbsolutePath()+"/convertedFile.dat");

                artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                currentSong.setArtist(artistName);

                songName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                currentSong.setName(songName);

                Intent intent = new Intent(MusicService.this, PlaybackActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
}
