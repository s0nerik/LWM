package com.lwm.app.model;

import android.content.Context;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.lib.NanoHTTPD;
import com.lwm.app.service.MusicService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class StreamServer extends NanoHTTPD {

    private HashSet<String> clients = new HashSet<>();
    private HashMap<String, Boolean> ready = new HashMap<>();
    private Context context;

    public StreamServer(Context context) {
        super(8888);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();

        Log.d("LWM", "serve:\nmethod: "+method+"\nuri: "+uri);

//        Map<String, String> headers = session.getHeaders();

        switch(method){
            case POST:
                if(uri.startsWith(App.IP)){
                    clients.add(uri.substring(App.IP.length()));

                    for(String client:clients){
                        Log.d(App.TAG, "client: "+client);
                    }

                }

                return new Response(Response.Status.OK, MIME_PLAINTEXT, "OK");

            case GET:
                switch(uri){

                    case App.STREAM:
                        Log.d(App.TAG, "StreamServer: STREAM");
                        FileInputStream fis = null;
                        try {

                            fis = new FileInputStream(MusicService.getCurrentLocalPlayer().getCurrentSource());

                        } catch (FileNotFoundException e) {e.printStackTrace();}

                        Response res = new Response(Response.Status.OK, "audio/x-mpeg", fis);
                        Random rnd = new Random();
                        String etag = Integer.toHexString( rnd.nextInt() );

                        res.addHeader("Connection", "Keep-alive");
                        res.addHeader("ETag", etag);
                        res.setChunkedTransfer(true);
                        return res;

                    case App.CURRENT_INFO:
                        Log.d(App.TAG, "StreamServer: CURRENT_INFO");
                        return new Response(Response.Status.OK, "application/json", getSongInfoJSON());

                    case App.CURRENT_POSITION:
                        Log.d(App.TAG, "StreamServer: CURRENT_POSITION");
                        return new Response(Response.Status.OK, MIME_PLAINTEXT, String.valueOf(MusicService.getCurrentLocalPlayer().getCurrentPosition()));

                    case App.CURRENT_ALBUMART:
                        Log.d(App.TAG, "StreamServer: CURRENT_ALBUMART");
                        InputStream is = null;
                        try {
                            is = context.getContentResolver().openInputStream(MusicService.getCurrentLocalPlayer().getCurrentAlbumArtUri());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        return new Response(Response.Status.OK, "image", is);
//                        if(path.endsWith(".png")){
//                            return new Response(Response.Status.OK, "image/png", is);
//                        }else if(path.endsWith(".jpg") || path.endsWith(".jpeg")){
//                            return new Response(Response.Status.OK, "image/jpeg", is);
//                        }else{
//                            return new Response(Response.Status.OK, "image", is);
//                        }
                }

            default:
                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Only POST and GET are supported.");
        }
    }

    private String getSongInfoJSON(){
        LocalPlayer mp = MusicService.getCurrentLocalPlayer();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("artist", mp.getCurrentArtist());
            jsonObject.put("title", mp.getCurrentTitle());
            jsonObject.put("album", mp.getCurrentAlbum());
            jsonObject.put("duration", mp.getCurrentDuration());
            jsonObject.put("duration_minutes", mp.getCurrentDurationInMinutes());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

}
