package com.lwm.app;

public class Constants {

    private Constants(){}

    public static final String TAG = "LWM";

    public static class Server {
        public static final String ADDRESS = "http://192.168.43.1:8888";
        public static final String IP = "/ip/";
        public static final String CURRENT_POSITION = "/position";
        public static final String CURRENT_INFO = "/info";
        public static final String STREAM = "/stream";
        public static final String PAUSE = "/pause";
        public static final String PLAY = "/play";
        public static final String SEEK_TO = "/seekTo/";
        public static final String CLIENT_READY = "/client_ready";
    }

    public static class Actions {
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

        // Stream actions
        public static final String ACTION_PLAY_STREAM = "com.lwm.player.action.PLAY_STREAM";
        public static final String ACTION_STREAM_NEXT_SONG = "com.lwm.player.action.PLAY_STREAM";
        public static final String ACTION_STREAM_PAUSE = "com.lwm.player.action.STREAM_PAUSE";
        public static final String ACTION_STREAM_UNPAUSE = "com.lwm.player.action.STREAM_UNPAUSE";
    }

    public static class Player {
        public static final String SONG_CHANGED = "song_changed";
        public static final String PLAYBACK_STARTED = "playback_started";
        public static final String PLAYBACK_PAUSED = "playback_paused";
        public static final String PLAYLIST_POSITION = "playlist_position";
        public static final String CURRENT_POSITION = "current_position";
        public static final String SEEK_POSITION = "seek_position";
        public static final String ALBUM_ART_URI = "album_art_uri";
    }

}
