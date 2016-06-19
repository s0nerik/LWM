package app.server
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.Injector
import app.R
import app.Utils
import app.models.Song
import app.players.LocalPlayer
import com.squareup.otto.Bus
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.ResponseException
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug

import javax.inject.Inject

import static fi.iki.elonen.NanoHTTPD.Method.GET
import static fi.iki.elonen.NanoHTTPD.Response.Status.BAD_REQUEST
import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR

@CompileStatic
class HttpStreamServer extends NanoHTTPD {

    static class Method {
        static String CURRENT_ALBUMART = "/albumart"
        static String STREAM = "/stream"
    }

    @Inject
    @PackageScope
    ContentResolver contentResolver

    @Inject
    @PackageScope
    LocalPlayer localPlayer

    @Inject
    @PackageScope
    Bus bus

    @Inject
    @PackageScope
    Utils utils

    HttpStreamServer(int port) {
        super(port)
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
                switch (uri) {
                    case Method.STREAM:
                        return stream(localPlayer.currentSong)
                    case Method.CURRENT_ALBUMART:
                        return currentAlbumArt(localPlayer.currentSong)
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

        Response res = new Response(Response.Status.OK, "audio/mpeg", fis)
        res.addHeader "Connection", "Keep-Alive"
        res.setChunkedTransfer(true);
        return res
    }

    private Response currentAlbumArt(Song song) {
        Debug.d "StreamServer: CURRENT_ALBUMART"
        InputStream is = null

        try {
            is = contentResolver.openInputStream song.albumArtUri
        } catch (ignore) {
            is = contentResolver.openInputStream utils.resourceToUri(R.drawable.no_cover)
        }

        def os = new ByteArrayOutputStream()
        is.withStream {
            BitmapFactory.decodeStream(it).compress(Bitmap.CompressFormat.WEBP, 80, os)
        }

        is = new ByteArrayInputStream(os.toByteArray())
        return new Response(Response.Status.OK, "image/webp", is)
    }

}
