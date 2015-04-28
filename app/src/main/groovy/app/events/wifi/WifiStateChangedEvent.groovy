package app.events.wifi;

import android.net.wifi.WifiManager
import groovy.transform.CompileStatic;

@CompileStatic
public class WifiStateChangedEvent {

    WifiManager wifiManager;

    public WifiStateChangedEvent(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }
}
