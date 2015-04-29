package app.server
import android.content.ContentResolver
import app.Injector
import app.player.LocalPlayer
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class StreamServer {

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
        this.player = player;
        Injector.inject(this);
    }

}
