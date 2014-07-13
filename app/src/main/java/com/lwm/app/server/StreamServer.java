package com.lwm.app.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;

import com.google.gson.Gson;
import com.lwm.app.App;
import com.lwm.app.lib.NanoHTTPD;
import com.lwm.app.model.Client;
import com.lwm.app.model.Song;
import com.lwm.app.player.BasePlayer;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.StreamPlayer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class StreamServer extends NanoHTTPD {

    public static final String SERVER_ADDRESS = "http://192.168.43.1:8888";
    public static final String CURRENT_POSITION = "/position";
    public static final String CURRENT_INFO = "/info";
    public static final String CURRENT_ALBUMART = "/albumart";
    public static final String STREAM = "/stream";
    public static final String PAUSE = "/pause";
    public static final String PLAY = "/play";
    public static final String PREPARE = "/prepare";
    public static final String PING = "/ping";
    public static final String SEEK_TO = "/seekTo/";
    public static final String CLIENT_READY = "/ready";
    public static final String CLIENT_REGISTER = "/register";
    public static final String CLIENT_UNREGISTER = "/unregister";

    private static Set<Client> clients = new HashSet<>();
    private static Set<Client> ready = new HashSet<>();
//    private boolean clientsCanManage = true;
    private BasePlayer player;
    private Context context;

    public StreamServer(Context context) {
        super(8888);
        this.context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences("server_prefs", Context.MODE_PRIVATE);
//        clientsCanManage = sharedPreferences.getBoolean("clients_can_manage", false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        String clientIP = session.getHeaders().get("remote-addr");

        Log.d("LWM", "serve:\nmethod: " + method + "\nuri: " + uri);

        switch(method){
            case POST: // Incoming info
                switch (uri) {
                    case PING:
                        return ping();
                    case PLAY:
                        return play();
                    case PAUSE:
                        return pause();
                    case PREPARE:
                        return prepare();

                    case CLIENT_REGISTER:
                        return registerClient(clientIP);
                    case CLIENT_UNREGISTER:
                        return unregisterClient(clientIP);
                    case CLIENT_READY:
                        return clientReady(clientIP);

                    default:
                        return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Bad request.");
                }

            case GET: // Outcoming info
                if(App.localPlayerActive()){
                    LocalPlayer localPlayer = App.getLocalPlayer();
                    Song song = localPlayer.getCurrentSong();
                    switch (uri) {
                        case STREAM:
                            return stream(song);
                        case CURRENT_INFO:
                            return currentInfo(song);
                        case CURRENT_POSITION:
                            return currentPosition(localPlayer);
                        case CURRENT_ALBUMART:
                            return currentAlbumArt(song);
                    }
                } else {
                    return new Response(Response.Status.NO_CONTENT, MIME_PLAINTEXT, "LocalPlayer isn't instantiated");
                }
            default:
                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Only POST and GET are supported.");
        }
    }

    private Response ping() {
        return new Response(Response.Status.OK, MIME_PLAINTEXT, "");
    }

    private Response play() {
        StreamPlayer streamPlayer = App.getMusicService().getStreamPlayer();
        Log.d(App.TAG, "StreamServer: PLAY");
        streamPlayer.start();
        return new Response(Response.Status.OK, MIME_PLAINTEXT, "Playback started.");
    }

    private Response pause() {
        StreamPlayer streamPlayer = App.getMusicService().getStreamPlayer();
        Log.d(App.TAG, "StreamServer: PAUSE");
        streamPlayer.pause();
        return new Response(Response.Status.OK, MIME_PLAINTEXT, "Playback paused.");
    }

    private Response prepare() {
        StreamPlayer streamPlayer = App.getMusicService().getStreamPlayer();
        Log.d(App.TAG, "StreamServer: PREPARE");
        streamPlayer.prepareNewSong();
        return new Response(Response.Status.OK, MIME_PLAINTEXT, "Preparation started.");
    }

    private Response registerClient(String clientIP) {
        clients.add(new Client(clientIP));
        Log.d(App.TAG, "--- CLIENTS ---");
        for (Client client : clients) {
            Log.d(App.TAG, "client: " + client.getIP());
        }
        return new Response(Response.Status.OK, MIME_PLAINTEXT, "Client added.");
    }

    private Response unregisterClient(String clientIP) {
        for (Client client : clients) {
            if (clientIP.equals(client.getIP())) {
                clients.remove(client);
                break;
            }
        }
        return new Response(Response.Status.OK, MIME_PLAINTEXT, "Client " + clientIP + " removed.");
    }

    private Response clientReady(String clientIP) {
        for (Client client : clients) {
            if (clientIP.equals(client.getIP())) {
                ready.add(client);
                break;
            }
        }
        return new Response(Response.Status.OK, MIME_PLAINTEXT, "Client " + clientIP + " is ready.");
    }

    private Response stream(Song song) {
        Log.d(App.TAG, "StreamServer: STREAM");
        FileInputStream fis = null;
        try {

            fis = new FileInputStream(song.getSource());

        } catch (FileNotFoundException e) {e.printStackTrace();}

        Response res = new Response(Response.Status.OK, "audio/x-mpeg", fis);
        res.addHeader("Connection", "Keep-Alive");
        res.setChunkedTransfer(true);
        return res;
    }

    private Response currentInfo(Song song) {
        Log.d(App.TAG, "StreamServer: CURRENT_INFO");
        return new Response(Response.Status.OK, "application/json", getSongInfoJSON(song));
    }

    private Response currentPosition(MediaPlayer player) {
        Log.d(App.TAG, "StreamServer: CURRENT_POSITION");
        return new Response(Response.Status.OK, MIME_PLAINTEXT, String.valueOf(player.getCurrentPosition()));
    }

    private Response currentAlbumArt(Song song) {
        Log.d(App.TAG, "StreamServer: CURRENT_ALBUMART");
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(song.getAlbumArtUri());
        } catch (FileNotFoundException ignored) {}
        return new Response(Response.Status.OK, "image", is);
    }

    public static boolean hasClients(){
        return !clients.isEmpty();
    }

    public static Set<Client> getClients(){
        return clients;
    }

    public static void addClient(Client client){
        clients.add(client);
    }

    public static void removeClient(Client client){
        clients.remove(client);
    }

    public static Set<Client> getReadyClients(){
        return ready;
    }

    public static int getNumberOfClients(){
        return clients.size();
    }

    private String getSongInfoJSON(Song song){
        return new Gson().toJson(song);
    }

}
