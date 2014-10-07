package com.lwm.app.websocket;

public interface SocketMessage {

    String START_FROM = "start_from";

    /**
     * String format for starting prom position
     */
    String FORMAT_START_FROM = START_FROM + ": %d";


    String SEEK_TO = "seek_to";

    /**
     * String format for seeking to position
     */
    String FORMAT_SEEK_TO = SEEK_TO + ": %d";


    String CURRENT_POSITION = "current_position";

    /**
     * String format for sending current position
     */
    String FORMAT_CURRENT_POSITION = CURRENT_POSITION + ": %d";


    String IS_PLAYING = "is_playing";

    /**
     * String format for sending current position
     */
    String FORMAT_IS_PLAYING = IS_PLAYING + ": %b";


    String OK = "ok";

    /**
     * String format for sending ok response
     */
    String FORMAT_OK = OK + ": %s";


    String ERROR = "error";

    /**
     * String format for sending error response
     */
    String FORMAT_ERROR = ERROR + ": %s";


    String PAUSE = "pause";
    String UNPAUSE = "unpause";
    String START = "start";
    String PREPARE = "prepare";
    String READY = "ready";
}