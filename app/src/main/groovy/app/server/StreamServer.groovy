package app.server
import android.content.ContentResolver
import android.util.Log
import app.Injector
import app.model.Song
import app.player.LocalPlayer
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import groovy.transform.CompileStatic
import ru.noties.debug.Debug

import javax.inject.Inject

@CompileStatic
public class StreamServer extends NanoHTTPD {

    public static final String PORT = "8888";

    static class Method {
        static String CURRENT_INFO = "/info";
        static String CURRENT_ALBUMART = "/albumart";
        static String STREAM = "/stream";
    }

    static class Url {
        static String SERVER_ADDRESS = "http://192.168.43.1:" + PORT;
        static String CURRENT_INFO = SERVER_ADDRESS + Method.CURRENT_INFO;
        static String CURRENT_ALBUMART = SERVER_ADDRESS + Method.CURRENT_ALBUMART;
        static String STREAM = SERVER_ADDRESS + Method.STREAM;
    }

    @Inject
    ContentResolver contentResolver;

    private LocalPlayer player;

    public StreamServer(LocalPlayer player) {
        super(8888);
        this.player = player;
        Injector.inject(this);
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
            case NanoHTTPD.Method.GET: // Outcoming info
                Song song = player.getCurrentSong();
                switch (uri) {
                    case Method.STREAM:
                        return stream(song);
                    case Method.CURRENT_INFO:
                        return currentInfo(song);
                    case Method.CURRENT_ALBUMART:
                        return currentAlbumArt(song);
                }
            default:
                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Only GET is supported.");
        }
    }

    private Response stream(Song song) {
        Debug.d("StreamServer: STREAM");
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
        Debug.d("StreamServer: CURRENT_INFO");
        return new Response(Response.Status.OK, "application/json", getSongInfoJSON(song));
    }

    private Response currentAlbumArt(Song song) {
        Debug.d("StreamServer: CURRENT_ALBUMART");
        InputStream is = null;
        try {
            is = contentResolver.openInputStream(song.getAlbumArtUri());
        } catch (FileNotFoundException ignored) {}
        return new Response(Response.Status.OK, "image", is);
    }

    private String getSongInfoJSON(Song song){
        return new Gson().toJson(song);
    }

}
