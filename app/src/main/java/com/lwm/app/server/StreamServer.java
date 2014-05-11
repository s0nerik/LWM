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
                if(PING.equals(uri)){
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "");
                } else if (PLAY.equals(uri)) {
                    StreamPlayer streamPlayer = App.getMusicService().getStreamPlayer();
                    Log.d(App.TAG, "StreamServer: PLAY");
                    streamPlayer.start();
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "Playback started.");
                } else if (PAUSE.equals(uri)) {
                    StreamPlayer streamPlayer = App.getMusicService().getStreamPlayer();
                    Log.d(App.TAG, "StreamServer: PAUSE");
                    streamPlayer.pause();
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "Playback paused.");
                } else if (PREPARE.equals(uri)) {
                    StreamPlayer streamPlayer = App.getMusicService().getStreamPlayer();
                    Log.d(App.TAG, "StreamServer: PREPARE");
                    streamPlayer.prepareNewSong();
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "Preparation started.");
                } else {
                    switch(uri){
                        case CLIENT_REGISTER:
                            clients.add(new Client(clientIP));
                            Log.d(App.TAG, "--- CLIENTS ---");
                            for (Client client : clients) {
                                Log.d(App.TAG, "client: " + client.getIP());
                            }
                            return new Response(Response.Status.OK, MIME_PLAINTEXT, "Client added.");

                        case CLIENT_UNREGISTER:
                            for(Client client:clients){
                                if(clientIP.equals(client.getIP())){
                                    clients.remove(client);
                                    break;
                                }
                            }
                            return new Response(Response.Status.OK, MIME_PLAINTEXT, "Client "+clientIP+" removed.");

                        case CLIENT_READY:
                            for(Client client:clients){
                                if(clientIP.equals(client.getIP())){
                                    ready.add(client);
                                    break;
                                }
                            }
                            return new Response(Response.Status.OK, MIME_PLAINTEXT, "Client "+clientIP+" is ready.");
                    }
                }

                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "");

            case GET: // Outcoming info
                if(App.localPlayerActive()){
                    LocalPlayer localPlayer = App.getMusicService().getLocalPlayer();
                    Song song = localPlayer.getCurrentSong();
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
                            } catch (FileNotFoundException ignored) {}
                            return new Response(Response.Status.OK, "image", is);
                    }
                } else {
                    return new Response(Response.Status.NO_CONTENT, MIME_PLAINTEXT, "LocalPlayer isn't instantiated");
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

    public static void addClient(Client client){
        clients.add(client);
    }

    public static void removeClient(Client client){
        clients.remove(client);
    }

    public static Set<Client> getReadyClients(){
        return ready;
    }

    public static int getNumberOfClients(){return clients.size();}

    private String getSongInfoJSON(Song song){
        return new Gson().toJson(song);
    }

}
