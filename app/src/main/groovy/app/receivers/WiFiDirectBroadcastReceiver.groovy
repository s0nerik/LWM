package app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.Injector
import app.events.p2p.P2PBroadcastReceivedEvent
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug

import javax.inject.Inject

@CompileStatic
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    @Inject
    @PackageScope
    Bus bus

    WiFiDirectBroadcastReceiver() {
        super()
        Injector.inject(this)
    }

    @Override
    void onReceive(Context context, Intent intent) {
        String action = intent.action
        Debug.d action
        bus.post new P2PBroadcastReceivedEvent(intent)
    }
}