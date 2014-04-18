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
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.StreamPlayer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class StreamServer extends NanoHTTPD {

    public static final String SERVER_ADDRESS = "http://192.168.43.1:8888";
    public static final String IP = "/ip/";
    public static final String CURRENT_POSITION = "/position";
    public static final String CURRENT_INFO = "/info";
    public static final String CURRENT_ALBUMART = "/albumart";
    public static final String STREAM = "/stream";
    public static final String PAUSE = "/pause";
    public static final String PLAY = "/play";
    public static final String PREPARE = "/prepare";
    public static final String PING = "/ping";
//    public static final String NEXT_SONG = "/next_song";
//    public static final String PREV_SONG = "/prev_song";
//    public static final String SONG_CHANGED = "/song_changed";
    public static final String SEEK_TO = "/seekTo/";
    public static final String CLIENT_READY = "/ready/";
//    public static final String TEST = "/test";

    private static HashSet<Client> clients = new HashSet<>();
    private static HashSet<Client> ready = new HashSet<>();
//    private boolean clientsCanManage = true;
    private BasePlayer player;
    private Context context;

    public StreamServer(Context context) {
        super(8888);
        this.context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences("server_prefs", Context.MODE_PRIVATE);
//        clientsCanManage = sharedPreferences.getBoolean("clients_can_manage", false);
//        clientsCanManage = true;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();

        Log.d("LWM", "serve:\nmethod: "+method+"\nuri: "+uri);

        switch(method){
            case POST: // Incoming info
                if(PING.equals(uri)) return new Response(Response.Status.OK, MIME_PLAINTEXT, "");

                else if(uri.startsWith(IP)) {
                    String ip = uri.substring(IP.length());
                    clients.add(new Client(ip));

                    Log.d(App.TAG, "--- CLIENTS ---");
                    for (Client client : clients) {
                        Log.d(App.TAG, "client: " + client.getIP());
                    }
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "Client added.");

                }else if(uri.startsWith(CLIENT_READY)){
                    String ip = uri.substring(CLIENT_READY.length());
                    for(Client client:clients){
                        if(ip.equals(client.getIP())){
                            ready.add(client);
                            break;
                        }
                    }
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "Client "+ip+" is ready.");
                }else{
                    StreamPlayer streamPlayer = App.getMusicService().getStreamPlayer();
                    switch(uri){
                        case PREPARE:
                            Log.d(App.TAG, "StreamServer: PREPARE");
                            streamPlayer.prepareNewSong();
                            return new Response(Response.Status.OK, MIME_PLAINTEXT, "Preparation started.");

                        case PLAY:
                            Log.d(App.TAG, "StreamServer: PLAY");
                            streamPlayer.start();
                            return new Response(Response.Status.OK, MIME_PLAINTEXT, "Playback started.");

                        case PAUSE:
                            Log.d(App.TAG, "StreamServer: PAUSE");
                            streamPlayer.pause();
                            return new Response(Response.Status.OK, MIME_PLAINTEXT, "Playback paused.");
                    }
                }

                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "");

            case GET: // Outcoming info
                LocalPlayer localPlayer = App.getMusicService().getLocalPlayer();
                Song song = LocalPlayer.getCurrentSong();
                switch(uri){

                    case STREAM:
                        Log.d(App.TAG, "StreamServer: STREAM");
                        FileInputStream fis = null;
                        try {

                            fis = new FileInputStream(song.getSource());

                        } catch (FileNotFoundException e) {e.printStackTrace();}

                        Response res = new Response(Response.Status.OK, "audio/x-mpeg", fis);
                        res.addHeader("Connection", "Keep-alive");
                        res.setChunkedTransfer(true);
                        return res;

                    case CURRENT_INFO:
                        Log.d(App.TAG, "StreamServer: CURRENT_INFO");
                        return new Response(Response.Status.OK, "application/json", getSongInfoJSON(song));

                    case CURRENT_POSITION:
                        Log.d(App.TAG, "StreamServer: CURRENT_POSITION");
                        return new Response(Response.Status.OK, MIME_PLAINTEXT, String.valueOf(localPlayer.getCurrentPosition()));

                    case CURRENT_ALBUMART:
                        Log.d(App.TAG, "StreamServer: CURRENT_ALBUMART");
                        InputStream is = null;
                        try {
                            is = context.getContentResolver().openInputStream(song.getAlbumArtUri());
                        } catch (FileNotFoundException e) {}
                        return new Response(Response.Status.OK, "image", is);
                }

            default:
                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Only POST and GET are supported.");
        }
    }

    public static boolean hasClients(){
        return !clients.isEmpty();
    }

    public static Set<Client> getClients(){
        return clients;
    }

    public static Set<Client> getReadyClients(){
        return ready;
    }

    public static int getNumberOfClients(){return clients.size();}

    private String getSongInfoJSON(Song song){
        return new Gson().toJson(song);
    }

}
