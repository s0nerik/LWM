package app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import app.App
import app.server.MusicStation
import com.github.s0nerik.betterknife.annotations.Profile
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
class MusicStationService extends Service {

    @Inject
    protected MusicStation musicStation

    @Profile
    @Override
    public void onCreate() {
        App.get().inject this
    }

    @Override
    public IBinder onBind(Intent intent) { null }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { START_NOT_STICKY }

}
