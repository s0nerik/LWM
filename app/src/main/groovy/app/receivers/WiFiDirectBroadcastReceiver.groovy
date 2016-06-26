package app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.App
import app.events.p2p.P2PBroadcastReceivedEvent
import com.github.s0nerik.rxbus.RxBus
import groovy.transform.CompileStatic
import ru.noties.debug.Debug

@CompileStatic
class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    WiFiDirectBroadcastReceiver() {
        super()
        App.get().inject(this)
    }

    @Override
    void onReceive(Context context, Intent intent) {
        String action = intent.action
        Debug.d action
        RxBus.post new P2PBroadcastReceivedEvent(intent)
    }
}