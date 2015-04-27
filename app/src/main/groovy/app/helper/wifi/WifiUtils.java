package app.helper.wifi;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import app.Injector;

import java.util.List;

import javax.inject.Inject;

public class WifiUtils {

    @Inject
    WifiManager wifiManager;

    @Inject
    ConnectivityManager connectivityManager;

    public WifiUtils() {
        Injector.inject(this);
    }

    public void connectToStation(String apName){
        String networkSSID = apName;
//        String networkPass = apPassword;

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";

        // TODO: password-protected station connection
//        conf.preSharedKey = "\""+ networkPass +"\"";

        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        wifiManager.addNetwork(conf);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();

                break;
            }
        }
    }

    public boolean isConnectedToStation() {
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi.isConnected();
    }

}
