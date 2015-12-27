package app.model

import android.net.Uri
import app.server.StreamServer
import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.codehaus.groovy.runtime.InvokerHelper

@Canonical
@CompileStatic
@EqualsAndHashCode(callSuper = true)
@Parcelable(exclude = {metaClass; albumArtUri; sourceUri})
class RemoteSong extends Song {

    String serverUrl

    RemoteSong(Song original, String serverUrl) {
        InvokerHelper.setProperties(this, original.properties)
        this.serverUrl = serverUrl
        source = "${serverUrl}${StreamServer.Method.STREAM}"
    }

    @Override
    Uri getAlbumArtUri() {
        Uri.parse("${serverUrl}${StreamServer.Method.CURRENT_ALBUMART}")
    }

    Uri getSourceUri() {
        Uri.parse source
    }
}
