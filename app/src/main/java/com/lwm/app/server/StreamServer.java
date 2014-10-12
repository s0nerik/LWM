package com.lwm.app.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.lwm.app.App;
import com.lwm.app.lib.NanoHTTPD;
import com.lwm.app.model.Song;
import com.lwm.app.service.LocalPlayerService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class StreamServer extends NanoHTTPD {

    public static final String PORT = "8888";

    public interface Method {
        String CURRENT_INFO = "/info";
        String CURRENT_ALBUMART = "/albumart";
        String STREAM = "/stream";
    }

    public interface Url {
        String SERVER_ADDRESS = "http://192.168.43.1:" + PORT;
        String CURRENT_INFO = SERVER_ADDRESS + Method.CURRENT_INFO;
        String CURRENT_ALBUMART = SERVER_ADDRESS + Method.CURRENT_ALBUMART;
        String STREAM = SERVER_ADDRESS + Method.STREAM;
    }

//    private boolean clientsCanManage = true;
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

        Map<String, String> files = new HashMap<String, String>();
        try {
            session.parseBody(files);
        } catch (IOException ioe) {
            return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        } catch (ResponseException re) {
            return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
        }
        Map<String, String> params = session.getParms();

        switch(method){
            case GET: // Outcoming info
                if(App.localPlayerActive()){
                    LocalPlayerService localPlayer = App.getLocalPlayerService();
                    Song song = localPlayer.getCurrentSong();
                    switch (uri) {
                        case Method.STREAM:
                            return stream(song);
                        case Method.CURRENT_INFO:
                            return currentInfo(song);
                        case Method.CURRENT_ALBUMART:
                            return currentAlbumArt(song);
                    }
                } else {
                    return new Response(Response.Status.NO_CONTENT, MIME_PLAINTEXT, "LocalPlayer isn't instantiated");
                }
            default:
                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Only GET is supported.");
        }
    }

    private Response stream(Song song) {
        Log.d(App.TAG, "StreamServer: STREAM");
        FileInputStream fis = null;
        try {

            fis = new FileInputStream(song.getSource());

        } catch (FileNotFoundException e) {e.printStackTrace();}

        Response res = new Response(Response.Status.OK, "audio/x-mpeg", fis);
        res.addHeader("Connection", "Keep-Alive");
//        res.setChunkedTransfer(true);
        return res;
    }

    private Response currentInfo(Song song) {
        Log.d(App.TAG, "StreamServer: CURRENT_INFO");
        return new Response(Response.Status.OK, "application/json", getSongInfoJSON(song));
    }

    private Response currentAlbumArt(Song song) {
        Log.d(App.TAG, "StreamServer: CURRENT_ALBUMART");
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(song.getAlbumArtUri());
        } catch (FileNotFoundException ignored) {}
        return new Response(Response.Status.OK, "image", is);
    }

    private String getSongInfoJSON(Song song){
        return new Gson().toJson(song);
    }

}
