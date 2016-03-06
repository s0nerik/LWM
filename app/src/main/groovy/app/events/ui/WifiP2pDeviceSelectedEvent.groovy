package app.events.ui

import android.net.wifi.p2p.WifiP2pDevice
import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
public class WifiP2pDeviceSelectedEvent {
    final WifiP2pDevice device
}
