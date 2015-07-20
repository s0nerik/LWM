package app.service
import android.app.Service
import android.content.Intent
import android.os.IBinder
import app.Injector
import app.server.MusicStation
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

    @Override
    public void onCreate() {
        Injector.inject this
        bus.register this

        musicStation.enable()
    }

    @Override
    public void onDestroy() {
        bus.unregister this
        musicStation.disable()
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY
    }

}
