package app.service
import android.app.Service
import android.content.Intent
import android.os.IBinder
import app.Injector
import app.server.MusicStation
import com.github.s0nerik.betterknife.annotations.Profile
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
class MusicStationService extends Service {

    @Inject
    @PackageScope
    Bus bus

    private MusicStation musicStation = new MusicStation()

    @Profile
    @Override
    public void onCreate() {
        Injector.inject this
        bus.register this
    }

    @Profile
    @Override
    public void onDestroy() { bus.unregister this }

    @Override
    public IBinder onBind(Intent intent) { null }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { START_NOT_STICKY }

}
