package com.lwm.app.events.wifi;

import android.net.wifi.WifiManager;

public class WifiStateChangedEvent {

    private WifiManager wifiManager;

    public WifiStateChangedEvent(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }
}
