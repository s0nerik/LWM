package app.server

import android.content.ContentResolver
import app.Injector
import app.Utils
import app.model.Song
import app.player.LocalPlayer
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.ResponseException
import fi.iki.elonen.NanoHTTPD.IHTTPSession
//import fi.iki.elonen.NanoHTTPD.*
import groovy.transform.CompileStatic

//import fi.iki.elonen.NanoHTTPD.*
import groovy.transform.PackageScope
import ru.noties.debug.Debug

import javax.inject.Inject

import static fi.iki.elonen.NanoHTTPD.Method.GET
import static fi.iki.elonen.NanoHTTPD.Response.Status.BAD_REQUEST
import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR

@CompileStatic
class StreamServer extends NanoHTTPD {

    static final String PORT = "8888"

    static class Method {
        static String CURRENT_INFO = "/info"
        static String CURRENT_ALBUMART = "/albumart"
        static String STREAM = "/stream"
    }

    static class Url {
        static String SERVER_ADDRESS = "http://192.168.43.1:${PORT}"
        static String CURRENT_INFO = SERVER_ADDRESS + Method.CURRENT_INFO
        static String CURRENT_ALBUMART = SERVER_ADDRESS + Method.CURRENT_ALBUMART
        static String STREAM = SERVER_ADDRESS + Method.STREAM
    }

    @Inject
    @PackageScope
    ContentResolver contentResolver

    private LocalPlayer player

    StreamServer(LocalPlayer player) {
        super(8888)
        this.player = player
        Injector.inject this
    }

    @Override
    Response serve(IHTTPSession session) {
        NanoHTTPD.Method method = session.method
        String uri = session.uri
        String clientIP = session.headers["remote-addr"]

        Debug.d "method: ${method}, uri: ${uri}"

        def files = [:]
        try {
            session.parseBody files
        } catch (IOException ioe) {
            return new Response(INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: ${ioe.message}")
        } catch (ResponseException re) {
            return new Response(re.status, MIME_PLAINTEXT, re.message)
        }
        def params = session.parms

        switch(method) {
            case GET: // Outcoming info
                Song song = player.currentSong
                switch (uri) {
                    case Method.STREAM:
                        return stream(song)
                    case Method.CURRENT_INFO:
                        return currentInfo(song)
                    case Method.CURRENT_ALBUMART:
                        return currentAlbumArt(song)
                }
            default:
                return new Response(BAD_REQUEST, MIME_PLAINTEXT, "Only GET is supported.")
        }
    }

    private Response stream(Song song) {
        Debug.d "StreamServer: STREAM"
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(song.source)
        } catch (FileNotFoundException e) { e.printStackTrace() }

        Response res = new Response(Response.Status.OK, "audio/x-mpeg", fis)
        res.addHeader "Connection", "Keep-Alive"
//        res.setChunkedTransfer(true);
        return res
    }

    private static Response currentInfo(Song song) {
        Debug.d "StreamServer: CURRENT_INFO"
        return new Response(Response.Status.OK, "application/json", getSongInfoJSON(song))
    }

    private Response currentAlbumArt(Song song) {
        Debug.d "StreamServer: CURRENT_ALBUMART"
        InputStream is = null
        try {
            is = contentResolver.openInputStream song.albumArtUri
        } catch (ignore) {}
        return new Response(Response.Status.OK, "image", is)
    }

    private static String getSongInfoJSON(Song song) { Utils.toJson song }

}
