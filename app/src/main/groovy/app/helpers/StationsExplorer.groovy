package app.helpers

import android.content.Context
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Handler
import app.App
import app.events.p2p.P2PBroadcastReceivedEvent
import app.events.p2p.StationsListUpdatedEvent
import app.models.Station
import app.models.StationInfo
import app.receivers.WiFiDirectBroadcastReceiver
import app.services.StreamPlayerService
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import ru.noties.debug.Debug

import javax.inject.Inject

import static android.net.wifi.p2p.WifiP2pManager.*
import static app.server.MusicStation.INSTANCE_NAME
import static app.server.MusicStation.SERVICE_TYPE
import static com.github.s0nerik.betterknife.dsl.AndroidContextDSL.intent
import static java.lang.reflect.Modifier.FINAL
import static java.lang.reflect.Modifier.PUBLIC
import static java.lang.reflect.Modifier.STATIC

@CompileStatic
class StationsExplorer {

    @Inject
    protected WifiP2pManager manager

    @Inject
    protected Context context

    @Inject
    protected Bus bus

    @Inject
    protected Handler handler

    WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(INSTANCE_NAME, SERVICE_TYPE)

    StationsExplorer() {
        App.get().inject(this)
        initIntentFilter()
    }

    private final IntentFilter intentFilter = new IntentFilter()
    private final WiFiDirectBroadcastReceiver receiver = new WiFiDirectBroadcastReceiver()

    private Channel channel

    private boolean discoveryStarted = false

    List<Station> stations = []

    void startStationsDiscovery() {
        if (discoveryStarted) return
        bus.register this
        context.registerReceiver receiver, intentFilter
        init()
        rediscover()
        discoveryStarted = true
    }

    void stopStationsDiscovery() {
        if (!discoveryStarted) return
        bus.unregister this
        context.unregisterReceiver receiver
        manager.stopPeerDiscovery channel, debugP2PActionListener("stopPeerDiscovery")
        discoveryStarted = false
    }

    ActionListener debugP2PActionListener(String methodName) {
        [
                onSuccess: { Debug.d "${methodName} success." },
                onFailure: { int code ->
                    // Command failed. Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    def cause = manager.class.declaredFields.find {
                        it.modifiers == (PUBLIC | STATIC | FINAL) && it.get(manager) == code}.name
                    Debug.e "${methodName} failure. Error: ${cause}."
                }
        ] as ActionListener
    }

    private void init() {
        channel = manager.initialize context, context.mainLooper, { Debug.d "Channel disconnected" }

        def txtListener = { String fullDomain, Map record, WifiP2pDevice device ->
            def station = stations.find {it.device == device}
            if (!station) {
                station = new Station(device, record as StationInfo)
                stations << station
            } else {
                station.info = record as StationInfo
            }
//            stations[device] = record as StationInfo
            Debug.d "txtListener: DnsSdTxtRecord available: ${record}"
        } as DnsSdTxtRecordListener

        def servListener = { String instanceName, String registrationType, WifiP2pDevice srcDevice ->
//            if (!stations.find {it.device == srcDevice}) {
//                stations << new Station(srcDevice)
//            }
            Debug.d "servListener: onBonjourServiceAvailable: ${instanceName}"
//            Debug.d "servListener: DnsSdTxtRecord available: ${srcDevice}"
//            srcDevice.deviceName = stations[srcDevice] ?: srcDevice.deviceName
        } as DnsSdServiceResponseListener

        manager.setDnsSdResponseListeners channel, servListener, txtListener
    }

    void rediscover() {
        manager.stopPeerDiscovery channel, debugP2PActionListener("stopPeerDiscovery")
        manager.discoverPeers channel, debugP2PActionListener("discoverPeers")

        manager.removeServiceRequest channel, serviceRequest, debugP2PActionListener("removeServiceRequest")
        manager.addServiceRequest channel, serviceRequest, debugP2PActionListener("addServiceRequest")
        manager.discoverServices channel, debugP2PActionListener("discoverServices")
    }

    void connect(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig()
        config.deviceAddress = device.deviceAddress
        config.wps.setup = WpsInfo.PBC

        manager.connect channel, config, debugP2PActionListener("connect")
    }

    private void initIntentFilter() {
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction WIFI_P2P_STATE_CHANGED_ACTION

        // Indicates a change in the list of available peers.
        intentFilter.addAction WIFI_P2P_PEERS_CHANGED_ACTION

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction WIFI_P2P_CONNECTION_CHANGED_ACTION

        // Indicates this device's details have changed.
        intentFilter.addAction WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
    }

    @Subscribe
    void onP2PBroadcastReceived(P2PBroadcastReceivedEvent event) {
        Debug.d "onP2PBroadcastReceived"

        switch (event.intent.action) {
            case WIFI_P2P_STATE_CHANGED_ACTION:
                Debug.d "WIFI_P2P_STATE_CHANGED_ACTION"
                break
            case WIFI_P2P_PEERS_CHANGED_ACTION:
                Debug.d "WIFI_P2P_PEERS_CHANGED_ACTION"
                handler.postDelayed({
                    manager.requestPeers channel, { WifiP2pDeviceList peerList ->
                        // Out with the old, in with the new.
                        stations.retainAll {
                            peerList.deviceList.contains(it.device)
                        }

                        bus.post new StationsListUpdatedEvent(stations)
                    }
                }, 1000)
                break
            case WIFI_P2P_CONNECTION_CHANGED_ACTION:
                Debug.d "WIFI_P2P_CONNECTION_CHANGED_ACTION"
                def networkInfo = event.intent.getParcelableExtra(EXTRA_NETWORK_INFO) as NetworkInfo

                if (networkInfo.connected) {

                    // We are connected with the other device, request connection
                    // info to find group owner IP

                    manager.requestConnectionInfo channel, { WifiP2pInfo info ->
                        Debug.d "onConnectionInfoAvailable: ${info}"

                        def uri = "ws://${info.groupOwnerAddress.hostAddress}:8080"
                        context.startService intent(context, StreamPlayerService).putExtra('uri', uri)
                    }
                }
                break
            case WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                Debug.d "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION"
                break
        }
    }

}