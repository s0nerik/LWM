package app.helper.wifi;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import app.Injector;
import app.Utils;
import app.events.access_point.AccessPointStateEvent;
import com.squareup.otto.Bus;

import java.lang.reflect.Method;

import javax.inject.Inject;

import static app.events.access_point.AccessPointStateEvent.State.CHANGING;
import static app.events.access_point.AccessPointStateEvent.State.DISABLED;
import static app.events.access_point.AccessPointStateEvent.State.ENABLED;

public class WifiAP {
    private static final int WIFI_AP_STATE_UNKNOWN = -1;
    public static String AP_NAME_REGEXP = "♪ (.{1,22}) #([0123456789abcdefABCDEF]{6})";
    public static String AP_NAME_FORMAT = "♪ %s %s"; // First - name, second - color
    private static int constant = 0;
    private static int WIFI_AP_STATE_DISABLING = 0;
    private static int WIFI_AP_STATE_DISABLED = 1;
    public static int WIFI_AP_STATE_ENABLING = 2;
    public static int WIFI_AP_STATE_ENABLED = 3;
    private static int WIFI_AP_STATE_FAILED = 4;
    private final String[] WIFI_STATE_TEXTSTATE = [
            "DISABLING", "DISABLED", "ENABLING", "ENABLED", "FAILED"
    ];

    @Inject
    Bus bus;
    @Inject
    WifiManager wifiManager;
    @Inject
    SharedPreferences preferences;

    private String TAG = "WifiAP";
    private String AP_NAME;
    private String AP_COLOR;
    private String AP_PASSWORD;
    private String AP_OPEN;
    private int stateWifiWasIn = -1;
    private boolean alwaysEnableWifi = true; //set to false if you want to try and set wifiManager state back to what it was before wifiManager ap enabling, true will result in the wifiManager always being enabled after wifiManager ap is disabled

    public WifiAP() {
        Injector.inject(this);
    }

    public void toggleWiFiAP() {
        AP_NAME = preferences.getString("ap_name", android.os.Build.MODEL);
        AP_COLOR = preferences.getString("ap_color", Utils.getRandomColorString());
        AP_PASSWORD = preferences.getString("ap_password", "12345678");

        boolean wifiApIsOn = getWifiAPState() == WIFI_AP_STATE_ENABLED || getWifiAPState() == WIFI_AP_STATE_ENABLING;
        new SetWifiAPTask(!wifiApIsOn, false).execute();
    }

    private int getWifiAPState() {
        int state = WIFI_AP_STATE_UNKNOWN;
        try {
            Method method2 = wifiManager.getClass().getMethod("getWifiApState");
            state = (Integer) method2.invoke(wifiManager);
        } catch (Exception e) {

        }

        if (state >= 10) {
            //using Android 4.0+ (or maybe 3+, haven't had a 3 device to test it on) so use states that are +10
            constant = 10;
        }

        //reset these in case was newer device
        WIFI_AP_STATE_DISABLING = 0 + constant;
        WIFI_AP_STATE_DISABLED = 1 + constant;
        WIFI_AP_STATE_ENABLING = 2 + constant;
        WIFI_AP_STATE_ENABLED = 3 + constant;
        WIFI_AP_STATE_FAILED = 4 + constant;

        Log.d(TAG, "getWifiAPState.state " + (state == -1 ? "UNKNOWN" : WIFI_STATE_TEXTSTATE[state - constant]));
        return state;
    }

    public boolean isEnabled() {
        return getWifiAPState() == WIFI_AP_STATE_ENABLED;
    }

    private int setWifiApEnabled(boolean enabled) {
        Log.d(TAG, "*** setWifiApEnabled CALLED **** " + enabled);

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = String.format(AP_NAME_FORMAT, AP_NAME, AP_COLOR);

        // TODO: Password-protected AP
//        config.preSharedKey = "\""+ AP_PASSWORD +"\"";

        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

        //remember wirelesses current state
        if (enabled && stateWifiWasIn == -1) {
            stateWifiWasIn = wifiManager.getWifiState();
        }

        //disable wireless
        if (enabled && wifiManager.getConnectionInfo() != null) {
            Log.d(TAG, "disable wifiManager: calling");
            wifiManager.setWifiEnabled(false);
            int loopMax = 10;
            while (loopMax > 0 && wifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLED) {
                Log.d(TAG, "disable wifiManager: waiting, pass: " + (10 - loopMax));
                try {
                    Thread.sleep(500);
                    loopMax--;
                } catch (Exception e) {

                }
            }
            Log.d(TAG, "disable wifiManager: done, pass: " + (10 - loopMax));
        }

        //enable/disable wifiManager ap
        int state = WIFI_AP_STATE_UNKNOWN;
        try {
            Log.d(TAG, (enabled ? "enabling" : "disabling") + " wifiManager ap: calling");
            wifiManager.setWifiEnabled(false);
            Method method1 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            //method1.invoke(wifiManager, null, enabled); // true
            method1.invoke(wifiManager, config, enabled); // true
            Method method2 = wifiManager.getClass().getMethod("getWifiApState");
            state = (Integer) method2.invoke(wifiManager);
        } catch (Exception e) {
            Log.e(Activity.WIFI_SERVICE, e.getMessage() != null ? e.getMessage() : "");
            // toastText += "ERROR " + e.getMessage();
        }


        //hold thread up while processing occurs
        if (!enabled) {
            int loopMax = 10;
            while (loopMax > 0 && (getWifiAPState() == WIFI_AP_STATE_DISABLING || getWifiAPState() == WIFI_AP_STATE_ENABLED || getWifiAPState() == WIFI_AP_STATE_FAILED)) {
                Log.d(TAG, (enabled ? "enabling" : "disabling") + " wifiManager ap: waiting, pass: " + (10 - loopMax));
                try {
                    Thread.sleep(500);
                    loopMax--;
                } catch (Exception e) {

                }
            }
            Log.d(TAG, (enabled ? "enabling" : "disabling") + " wifiManager ap: done, pass: " + (10 - loopMax));

            //enable wifiManager if it was enabled beforehand
            //this is somewhat unreliable and app gets confused and doesn't turn it back on sometimes so added toggle to always enable if you desire
            if (stateWifiWasIn == WifiManager.WIFI_STATE_ENABLED || stateWifiWasIn == WifiManager.WIFI_STATE_ENABLING || stateWifiWasIn == WifiManager.WIFI_STATE_UNKNOWN || alwaysEnableWifi) {
                Log.d(TAG, "enable wifiManager: calling");
                wifiManager.setWifiEnabled(true);
                //don't hold things up and wait for it to get enabled
            }

            stateWifiWasIn = -1;
        } else if (enabled) {
            int loopMax = 10;
            while (loopMax > 0 && (getWifiAPState() == WIFI_AP_STATE_ENABLING || getWifiAPState() == WIFI_AP_STATE_DISABLED || getWifiAPState() == WIFI_AP_STATE_FAILED)) {
                Log.d(TAG, (enabled ? "enabling" : "disabling") + " wifiManager ap: waiting, pass: " + (10 - loopMax));
                try {
                    Thread.sleep(500);
                    loopMax--;
                } catch (Exception e) {

                }
            }
            Log.d(TAG, (enabled ? "enabling" : "disabling") + " wifiManager ap: done, pass: " + (10 - loopMax));
        }
        return state;
    }

    class SetWifiAPTask extends AsyncTask<Void, Void, Void> {
        boolean mMode; //enable or disable wifiManager AP
        boolean mFinish; //finalize or not (e.g. on exit)

        public SetWifiAPTask(boolean mode, boolean finish) {
            mMode = mode;
            mFinish = finish;
        }

        @Override
        protected Void doInBackground(Void... params) {
            setWifiApEnabled(mMode);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            bus.post(new AccessPointStateEvent(CHANGING));

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            bus.post(new AccessPointStateEvent(mMode? ENABLED : DISABLED));

        }
    }

}