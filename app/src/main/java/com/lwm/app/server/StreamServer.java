package com.lwm.app.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;
import com.lwm.app.App;
import com.lwm.app.lib.NanoHTTPD;
import com.lwm.app.model.Client;
import com.lwm.app.model.Song;
import com.lwm.app.player.BasePlayer;
import com.lwm.app.server.async.tasks.SongInfoGetter;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.service.StreamPlayerService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StreamServer extends NanoHTTPD {

    public static final String PORT = "8888";

    public interface Method {
        String CURRENT_POSITION = "/position";
        String CURRENT_INFO = "/info";
        String CURRENT_ALBUMART = "/albumart";
        String STREAM = "/stream";
        String PAUSE = "/pause";
        String UNPAUSE = "/unpause";
        String START = "/start";
        String START_FROM = "/startFromPosition";
        String PREPARE = "/prepare";
        String PING = "/ping";
        String SEEK_TO = "/seekToPosition";
        String CLIENT_READY = "/ready";
        String CLIENT_REGISTER = "/register";
        String CLIENT_UNREGISTER = "/unregister";
        String IS_PLAYING = "/is_playing";
    }

    public interface Url {
        String SERVER_ADDRESS = "http://192.168.43.1:" + PORT;
        String CURRENT_POSITION = SERVER_ADDRESS + Method.CURRENT_POSITION;
        String CURRENT_INFO = SERVER_ADDRESS + Method.CURRENT_INFO;
        String CURRENT_ALBUMART = SERVER_ADDRESS + Method.CURRENT_ALBUMART;
        String STREAM = SERVER_ADDRESS + Method.STREAM;
        String PAUSE = SERVER_ADDRESS + Method.PAUSE;
        String UNPAUSE = SERVER_ADDRESS + Method.UNPAUSE;
        String START = SERVER_ADDRESS + Method.START;
        String START_FROM = SERVER_ADDRESS + Method.START_FROM;
        String PREPARE = SERVER_ADDRESS + Method.PREPARE;
        String PING = SERVER_ADDRESS + Method.PING;
        String SEEK_TO = SERVER_ADDRESS + Method.SEEK_TO;
        String CLIENT_READY = SERVER_ADDRESS + Method.CLIENT_READY;
        String CLIENT_REGISTER = SERVER_ADDRESS + Method.CLIENT_REGISTER;
        String CLIENT_UNREGISTER = SERVER_ADDRESS + Method.CLIENT_UNREGISTER;
        String IS_PLAYING = SERVER_ADDRESS + Method.IS_PLAYING;
    }

    public interface Params {
        String POSITION = "position";
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

        Log.d("LWM", "\nserve:\nmethod: " + method + "\nuri: " + uri);

        switch(method){
            case POST: // Incoming info
                switch (uri) {
                    case Method.START:
                        return play();
                    case Method.START_FROM:

                        Map<String, String> files = new HashMap<String, String>();
                        try {
                            session.parseBody(files);
                        } catch (IOException ioe) {
                            return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                        } catch (ResponseException re) {
                            return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
                        }
                        Map<String, String> params = session.getParms();
                        int pos = Integer.parseInt(params.get(Params.POSITION));

                        return playFrom(pos);
                    case Method.PAUSE:
                        return pause();
                    case Method.PREPARE:
                        return prepare();

                    case Method.CLIENT_REGISTER:
                        return registerClient(clientIP);
                    case Method.CLIENT_UNREGISTER:
                        return unregisterClient(clientIP);

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
                        case Method.IS_PLAYING:
                            return isPlaying(localPlayer);
                    }
                } else {
                    return new Response(Response.Status.NO_CONTENT, MIME_PLAINTEXT, "LocalPlayer isn't instantiated");
                }
            default:
                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Only POST and GET are supported.");
        }
    }

    // Client methonds
    private Response playFrom(int pos) {
        StreamPlayerService streamPlayer = App.getStreamPlayerService();
        Log.d(App.TAG, "StreamServer: START_FROM");
        streamPlayer.seekTo(pos);
        streamPlayer.start();

        new SongInfoGetter(streamPlayer.getPlayer()).execute();

        return new Response(Response.Status.OK, MIME_PLAINTEXT, "Playback started from: "+pos);
    }

    private Response play() {
        StreamPlayerService streamPlayer = App.getStreamPlayerService();
        Log.d(App.TAG, "StreamServer: START");
        streamPlayer.start();

        new SongInfoGetter(streamPlayer.getPlayer()).execute();

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
        return new Response(Response.Status.OK, MIME_PLAINTEXT, "Prepared.");
    }

    // Server methods
    private Response registerClient(String clientIP) {
        final Client client = new Client(clientIP);
        clients.add(client);

        if (App.getLocalPlayerService().isPlaying()) {
            client.prepare(context).setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    if (e != null) clients.remove(client);
                    else client.startFrom(context, App.getLocalPlayerService().getCurrentPosition());
                }
            });
        }

        Log.d(App.TAG, "--- CLIENTS ---");
        for (Client c : clients) {
            Log.d(App.TAG, "client: " + c.getIP());
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

    private Response isPlaying(LocalPlayerService localPlayer) {
        return new Response(Boolean.toString(localPlayer.isPlaying()));
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
