package com.lwm.app.model;

import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.lib.NanoHTTPD;
import com.lwm.app.service.MusicService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class StreamServer extends NanoHTTPD {

    private HashSet<String> clients = new HashSet<>();
    private HashMap<String, Boolean> ready = new HashMap<>();

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

                        res.addHeader("Connection", "Keep-alive");
                        res.addHeader("ETag", etag);
                        res.setChunkedTransfer(true);
                        return res;

                    case App.CURRENT_INFO:
                        Log.d(App.TAG, "StreamServer: CURRENT_INFO");
//                        return new Response(Response.Status.OK, "application/xml", getSongInfoXml());
                        return new Response(Response.Status.OK, "application/json", getSongInfoJSON());

                    case App.CURRENT_POSITION:
                        Log.d(App.TAG, "StreamServer: CURRENT_POSITION");
                        return new Response(Response.Status.OK, MIME_PLAINTEXT, String.valueOf(MusicService.getCurrentPlayer().getCurrentPosition()));
                }

            default:
                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Only POST and GET are supported.");
        }
    }

//    private String getSongInfoXml(){
//        XmlSerializer serializer = Xml.newSerializer();
//        StringWriter writer = new StringWriter();
//        MusicPlayer mp = MusicService.getCurrentPlayer();
//        try {
//            serializer.setOutput(writer);
//
//            serializer.startDocument("UTF-8", true);
//                serializer.startTag("", "song");
//
//                    serializer.startTag("", "artist");
//                        serializer.text(mp.getCurrentArtist());
//                    serializer.endTag("", "artist");
//
//                    serializer.startTag("", "title");
//                        serializer.text(mp.getCurrentTitle());
//                    serializer.endTag("", "title");
//
//                    serializer.startTag("", "album");
//                        serializer.text(mp.getCurrentAlbum());
//                    serializer.endTag("", "album");
//
//                    serializer.startTag("", "duration");
//                        serializer.text(mp.getCurrentDurationInMinutes());
//                    serializer.endTag("", "duration");
//
//                serializer.endTag("", "song");
//            serializer.endDocument();
//
//            return writer.toString();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    private String getSongInfoJSON(){
        MusicPlayer mp = MusicService.getCurrentPlayer();
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
