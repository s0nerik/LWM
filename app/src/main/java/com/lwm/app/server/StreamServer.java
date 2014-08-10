package com.lwm.app.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.lwm.app.App;
import com.lwm.app.lib.NanoHTTPD;
import com.lwm.app.model.Client;
import com.lwm.app.model.Song;
import com.lwm.app.player.BasePlayer;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.service.StreamPlayerService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class StreamServer extends NanoHTTPD {

    public interface Method {
        String CURRENT_POSITION = "/position";
        String CURRENT_INFO = "/info";
        String CURRENT_ALBUMART = "/albumart";
        String STREAM = "/stream";
        String PAUSE = "/pause";
        String PLAY = "/play";
        String PREPARE = "/prepare";
        String PING = "/ping";
        String SEEK_TO = "/seekTo/";
        String CLIENT_READY = "/ready";
        String CLIENT_REGISTER = "/register";
        String CLIENT_UNREGISTER = "/unregister";
    }

    public interface Url {
        String SERVER_ADDRESS = "http://192.168.43.1:8888";
        String CURRENT_POSITION = SERVER_ADDRESS + "/position";
        String CURRENT_INFO = SERVER_ADDRESS + "/info";
        String CURRENT_ALBUMART = SERVER_ADDRESS + "/albumart";
        String STREAM = SERVER_ADDRESS + "/stream";
        String PAUSE = SERVER_ADDRESS + "/pause";
        String PLAY = SERVER_ADDRESS + "/play";
        String PREPARE = SERVER_ADDRESS + "/prepare";
        String PING = SERVER_ADDRESS + "/ping";
        String SEEK_TO = SERVER_ADDRESS + "/seekTo/";
        String CLIENT_READY = SERVER_ADDRESS + "/ready";
        String CLIENT_REGISTER = SERVER_ADDRESS + "/register";
        String CLIENT_UNREGISTER = SERVER_ADDRESS + "/unregister";
    }

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
        NanoHTTPD.Method method = session.getMethod();
        String uri = session.getUri();
        String clientIP = session.getHeaders().get("remote-addr");

        Log.d("LWM", "serve:\nmethod: " + method + "\nuri: " + uri);

        switch(method){
            case POST: // Incoming info
                switch (uri) {
                    case Method.PING:
                        return ping();
                    case Method.PLAY:
                        return play();
                    case Method.PAUSE:
                        return pause();
                    case Method.PREPARE:
                        return prepare();

                    case Method.CLIENT_REGISTER:
                        return registerClient(clientIP);
                    case Method.CLIENT_UNREGISTER:
                        return unregisterClient(clientIP);
                    case Method.CLIENT_READY:
                        return clientReady(clientIP);

                    default:
                        return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Bad request.");
                }

            case GET: // Outcoming info
                if(App.localPlayerActive()){
                    LocalPlayerService localPlayer = App.getLocalPlayerService();
                    Song song = localPlayer.getCurrentSong();
                    switch (uri) {
                        case Method.STREAM:
                            return stream(song);
                        case Method.CURRENT_INFO:
                            return currentInfo(song);
                        case Method.CURRENT_POSITION:
                            return currentPosition(localPlayer);
                        case Method.CURRENT_ALBUMART:
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
        return new Response(Response.Status.OK, MIME_PLAINTEXT, "Ping");
    }

    private Response play() {
        StreamPlayerService streamPlayer = App.getStreamPlayerService();
        Log.d(App.TAG, "StreamServer: PLAY");
        streamPlayer.start();
        return new Response(Response.Status.OK, MIME_PLAINTEXT, "Playback started.");
    }

    private Response pause() {
        StreamPlayerService streamPlayer = App.getStreamPlayerService();
        Log.d(App.TAG, "StreamServer: PAUSE");
        streamPlayer.pause();
        return new Response(Response.Status.OK, MIME_PLAINTEXT, "Playback paused.");
    }

    private Response prepare() {
        StreamPlayerService streamPlayer = App.getStreamPlayerService();
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

    private Response currentPosition(LocalPlayerService player) {
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
