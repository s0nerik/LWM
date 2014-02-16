package com.lwm.app.model;

import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.lib.NanoHTTPD;
import com.lwm.app.service.MusicService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Random;

public class StreamServer extends NanoHTTPD {

    private HashSet<String> clients = new HashSet<>();

    public StreamServer() {
        super(8888);
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

                            fis = new FileInputStream(MusicService.getCurrentPlayer().getCurrentSource());

                        } catch (FileNotFoundException e) {e.printStackTrace();}

                        Response res = new Response(Response.Status.OK, "audio/x-mpeg", fis);
                        Random rnd = new Random();
                        String etag = Integer.toHexString( rnd.nextInt() );

                        res.addHeader( "Connection", "Keep-alive");
                        res.addHeader( "ETag", etag);
                        res.setChunkedTransfer(true);
                        return res;

                    case App.CURRENT_POSITION:
                        Log.d(App.TAG, "StreamServer: CURRENT_POSITION");
                        return new Response(Response.Status.OK, MIME_PLAINTEXT, String.valueOf(MusicService.getCurrentPlayer().getCurrentPosition()));
                }

            default:
                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Only POST and GET are supported.");
        }
    }

}
