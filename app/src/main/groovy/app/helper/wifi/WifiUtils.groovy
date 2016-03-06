package app.helper.wifi
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import app.Injector
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
public class WifiUtils {

    @Inject
    @PackageScope
    WifiManager wifiManager

    @Inject
    @PackageScope
    ConnectivityManager connectivityManager

    WifiUtils() {
        Injector.inject(this)
    }

    void connectToStation(String apName){
        String networkSSID = apName;
//        String networkPass = apPassword;

        WifiConfiguration conf = new WifiConfiguration()
        conf.SSID = '"networkSSID"'

        // TODO: password-protected station connection
//        conf.preSharedKey = "\""+ networkPass +"\"";

        conf.allowedKeyManagement.set WifiConfiguration.KeyMgmt.NONE

        wifiManager.addNetwork conf

        def list = wifiManager.configuredNetworks
        for( WifiConfiguration i : list ) {
            if('"networkSSID"' == i.SSID) {
                wifiManager.disconnect()
                wifiManager.enableNetwork i.networkId, true
                wifiManager.reconnect()

                break
            }
        }
    }

    boolean isConnectedToStation() {
        NetworkInfo wifi = connectivityManager.getNetworkInfo ConnectivityManager.TYPE_WIFI
        return wifi.connected
    }

}
