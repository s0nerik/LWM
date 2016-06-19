package app.models

import android.net.wifi.p2p.WifiP2pDevice
import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class Station {
    WifiP2pDevice device
    StationInfo info
}