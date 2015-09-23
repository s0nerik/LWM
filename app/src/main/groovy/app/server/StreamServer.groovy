package app.server
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.Injector
import app.R
import app.Utils
import app.events.player.playback.SongChangedEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.model.Song
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
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
class StreamServer extends NanoHTTPD {

    static final String PORT = "8888"

    static class Method {
        static String CURRENT_INFO = "/info"
        static String CURRENT_ALBUMART = "/albumart"
        static String STREAM = "/stream"
    }

    private Song song

    @Inject
    @PackageScope
    ContentResolver contentResolver

    @Inject
    @PackageScope
    Bus bus

    @Inject
    @PackageScope
    Utils utils

    StreamServer() {
        super(8888)
        Injector.inject this
    }

    @Override
    void start() throws IOException {
        super.start()
        bus.register this
    }

    @Override
    void stop() {
        bus.unregister this
        super.stop()
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

        Response res = new Response(Response.Status.OK, "audio/mpeg", fis)
        res.addHeader "Connection", "Keep-Alive"
        res.setChunkedTransfer(true);
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

    private static String getSongInfoJSON(Song song) { song.toJson() }

    @Subscribe
    void onSongChanged(SongChangedEvent event) {
        song = event.song
    }

    @Subscribe
    void onSongAvailable(CurrentSongAvailableEvent event) {
        song = event.song
    }

}
