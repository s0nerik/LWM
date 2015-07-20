package app.ui.fragment
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import app.R
import app.adapter.StationsAdapter
import app.adapter.WiFiP2pDevicesAdapter
import app.events.client.SocketOpenedEvent
import app.events.p2p.P2PBroadcastReceivedEvent
import app.events.ui.WifiP2pDeviceSelectedEvent
import app.helper.wifi.WifiUtils
import app.receiver.WiFiDirectBroadcastReceiver
import app.ui.activity.RemotePlaybackActivity
import app.ui.base.DaggerFragment
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug

import javax.inject.Inject

import static android.net.wifi.p2p.WifiP2pManager.*

@CompileStatic
@InjectLayout(R.layout.page_stations_around)
public class FindStationsFragment extends DaggerFragment {

    @Inject
    @PackageScope
    Bus bus

    @Inject
    @PackageScope
    WifiUtils wifiUtils

//    @Inject
//    @PackageScope
//    WifiManager wifiManager

    @Inject
    @PackageScope
    WifiP2pManager manager

    @Inject
    @PackageScope
    ConnectivityManager connectivityManager

    @Inject
    @PackageScope
    Handler handler

//    @InjectView(R.id.twoWayView)
//    ListView mListView
    @InjectView(R.id.refreshLayout)
    SwipeRefreshLayout mRefreshLayout
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar
    @InjectView(R.id.btnRefresh)
    Button mBtnRefresh
    @InjectView(R.id.emptyView)
    LinearLayout mEmptyView
    @InjectView(R.id.recycler)
    RecyclerView recycler

    private List<ScanResult> scanResults
    private StationsAdapter stationsAdapter

    private Channel channel
    private WiFiDirectBroadcastReceiver receiver
    private List<WifiP2pDevice> peers = new ArrayList<>()

    private final IntentFilter intentFilter = new IntentFilter()

    private WiFiP2pDevicesAdapter adapter

    private boolean isRefreshing = false

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        bus.register this

        initIntentFilter()

        channel = manager.initialize activity, activity.mainLooper, null
    }

    @Override
    void onDestroy() {
        super.onDestroy()
        bus.unregister this
    }

    @Override
    void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)

        adapter = new WiFiP2pDevicesAdapter(peers)

        recycler.layoutManager = new LinearLayoutManager(activity)
        recycler.adapter = adapter

        mRefreshLayout.setColorSchemeResources(
                R.color.pull_to_refresh_1,
                R.color.pull_to_refresh_2,
                R.color.pull_to_refresh_3,
                R.color.pull_to_refresh_4
        )
//        mRefreshLayout.onRefreshListener = { startScanningStations true }
//
//        if (!savedInstanceState) {
//            startScanningStations false
//        }
    }

    @Override
    void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver()
        activity.registerReceiver receiver, intentFilter

        manager.discoverPeers channel,
                [onSuccess: { Debug.d "discoverPeers onSuccess" },
                 onFailure: { int reason ->
                     Debug.d "discoverPeers onFailure (${reason})"
                 }] as ActionListener
    }

    @Override
    void onPause() {
        super.onPause()
        activity.unregisterReceiver receiver
    }

//    @OnItemClick(R.id.twoWayView)
//    void onStationClicked(int position) {
//        String ap = stationsAdapter.getItem(position).SSID
////        new StationConnectionTask().execute ap
//    }

//    @OnClick(R.id.btnRefresh)
//    void onRefreshStations(View v) {
//        startScanningStations false
//    }

    private boolean isWifiNetworkAvailable() {
        NetworkInfo activeNetworkInfo = connectivityManager.getNetworkInfo ConnectivityManager.TYPE_WIFI
        return activeNetworkInfo?.connected
    }

//    @Subscribe
//    public void onScanResultsAvailable(WifiScanResultsAvailableEvent event) {
//        Debug.d "setScanResults()"
//        scanResults = event.scanResults
//
//        stationsAdapter = new StationsAdapter(activity, scanResults)
//
//        if (isRefreshing) {
//            mRefreshLayout.refreshing = false
//            isRefreshing = false
//        }
//
//        mProgressBar.visibility = View.GONE
//        if (stationsAdapter.count > 0) {
//            mListView.visibility = View.VISIBLE
//            mListView.adapter = stationsAdapter
//        } else {
//            mListView.visibility = View.GONE
//            mEmptyView.visibility = View.VISIBLE
//        }
//    }

//    // For debug only
//    private ScanResult createFakeScanResult() {
//        try {
//            Constructor<ScanResult> ctor = ScanResult.getDeclaredConstructor(null)
//            ctor.accessible = true
//            ScanResult sr = ctor.newInstance null
//            sr.BSSID = "foo"
//            sr.SSID = String.format(WifiAP.AP_NAME_FORMAT, "Ololo", "#000000")
//            sr.level = 3
//            return sr
//        } catch (SecurityException | InvocationTargetException | NoSuchMethodException | java.lang.InstantiationException | IllegalAccessException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace()
//        }
//        return null
//    }

    protected void startStreamActivity() {
        Intent intent = new Intent(activity, RemotePlaybackActivity)
        activity.startActivity intent
    }

    private void startRegistration() {
        //  Create a string map containing information about your service.
        def record = [listen_port: "8080",
                      station_name : "station",
                      current_song : "Asking Alexandria - Closure"]

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        def serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance "_test", "_presence._tcp", record

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        manager.addLocalService channel, serviceInfo,
                [onSuccess: {
                    Snackbar.make(view, "addLocalService onSuccess", Snackbar.LENGTH_LONG).show()
                    Debug.d "addLocalService onSuccess"
                },
                 onFailure: {int arg0 -> Debug.d "addLocalService onFailure" }] as ActionListener
    }

    private void discoverService() {
        def buddies = [:]

        def txtListener = { String fullDomain, Map record, WifiP2pDevice device ->
            /* Callback includes:
             * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
             * record: TXT record dta as a map of key/value pairs.
             * device: The device running the advertised service.
             */

            Snackbar.make(view, "DnsSdTxtRecord available: ${record}", Snackbar.LENGTH_LONG).show()
            Debug.d "DnsSdTxtRecord available: ${record}"
            buddies[device.deviceAddress] = record["buddyname"]
        } as DnsSdTxtRecordListener


        def servListener = { String instanceName, String registrationType, WifiP2pDevice resourceType ->

            // Update the device name with the human-friendly version from
            // the DnsTxtRecord, assuming one arrived.
            resourceType.deviceName = buddies[resourceType.deviceAddress] ?: resourceType.deviceName

//            // Add to the custom adapter defined specifically for showing
//            // wifi devices.
//            WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager()
//                    .findFragmentById(R.id.frag_peerlist);
//            WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment
//                    .getListAdapter());
//
//            adapter.add(resourceType);
//            adapter.notifyDataSetChanged();
//            Snackbar.make(view, "onBonjourServiceAvailable: ${instanceName}", Snackbar.LENGTH_LONG).show()
            Debug.d "onBonjourServiceAvailable: ${instanceName}"
        } as DnsSdServiceResponseListener

        manager.setDnsSdResponseListeners channel, servListener, txtListener


        def serviceRequest = WifiP2pDnsSdServiceRequest.newInstance()
        manager.addServiceRequest channel, serviceRequest,
                [onSuccess: { Debug.d "serviceRequest onSuccess" },
                 onFailure: { int code ->
                     // Command failed. Check for P2P_UNSUPPORTED, ERROR, or BUSY
                     Debug.d "serviceRequest onFailure, code: ${code}"
                 }] as ActionListener

        manager.discoverServices channel,
                [onSuccess: { Debug.d "discoverServices onSuccess" },
                 onFailure: { int code ->
                     // Command failed. Check for P2P_UNSUPPORTED, ERROR, or BUSY
                     Debug.d "discoverServices onFailure, code: ${code}"
                 }] as ActionListener
    }


//    private void startScanningStations(boolean isPulled) {
//        wifiManager.startScan()
//        if (isPulled) {
//            mProgressBar.visibility = View.GONE
//            isRefreshing = true
//        } else {
//            mProgressBar.visibility = View.VISIBLE
//            mListView.visibility = View.GONE
//        }
//        mEmptyView.visibility= View.GONE
//    }
//
//    private Observable<Void> connectStation(String ap) {
//        if (wifiManager?.wifiState == WifiManager.WIFI_STATE_ENABLED) {
//            WifiInfo info = wifiManager.connectionInfo
//            if (!info || ap != info.SSID) {
//                // Device is connected to different AP or not connected at all
//                wifiUtils.connectToStation ap
//            }
//        }
//
//
////        Observable.just {
////            if (wifiManager?.wifiState == WifiManager.WIFI_STATE_ENABLED) {
////                WifiInfo info = wifiManager.connectionInfo
////                if (!info || ap != info.SSID) {
////                    // Device is connected to different AP or not connected at all
////                    wifiUtils.connectToStation ap
////                    while (!wifiNetworkAvailable) {
////                        try {
////                            Thread.sleep 1000
////                        } catch (InterruptedException e) {
////                            e.printStackTrace()
////                        }
////                    }
////
////                    while (true) {
////                        try {
////                            httpclient.execute httpPostPing
////                            break
////                        } catch (IOException ignored) {
////                            try {
////                                Thread.sleep 1000
////                            } catch (InterruptedException e) {
////                                e.printStackTrace()
////                            }
////                        }
////                    }
////                }
////            }
////        } as Void
//    }
//
////    private static class StationConnectionTask extends AsyncTask<String, Void, Void> {
////        HttpClient httpclient = new DefaultHttpClient()
////        HttpPost httpPostPing = new HttpPost(StreamServer.Url.SERVER_ADDRESS)
////
////        ProgressDialog progressDialog
////
////
////        @Override
////        protected void onPreExecute() {
////            progressDialog = new ProgressDialog(activity)
////            progressDialog.message = resources.getString R.string.connecting_to_the_station
////            progressDialog.show()
////        }
////
////        @Override
////        protected Void doInBackground(String... params) {
////            String ap = params[0];
//////            String pass = params[1];
////
////            if (wifiManager?.wifiState == WifiManager.WIFI_STATE_ENABLED) {
////                WifiInfo info = wifiManager.connectionInfo
////                if (!info || ap != info.SSID) {
////                    // Device is connected to different AP or not connected at all
////                    wifiUtils.connectToStation ap
////                    while (!wifiNetworkAvailable) {
////                        try {
////                            Thread.sleep 1000
////                        } catch (InterruptedException e) {
////                            e.printStackTrace()
////                        }
////                    }
////
////                    while (true) {
////                        try {
////                            httpclient.execute httpPostPing
////                            break
////                        } catch (IOException ignored) {
////                            try {
////                                Thread.sleep 1000
////                            } catch (InterruptedException e) {
////                                e.printStackTrace()
////                            }
////                        }
////                    }
////                }
////            }
////            return null;
////        }
////
////        @Override
////        protected void onPostExecute(Void aVoid) {
////            progressDialog.dismiss()
////            activity.startService new Intent(activity, StreamPlayerService)
////            bus.post new ShouldStartWebSocketClientEvent()
////        }
////    }

    @Subscribe
    void onDeviceSelected(WifiP2pDeviceSelectedEvent event) {
        connect peers.indexOf(event.device)
    }

    @Subscribe
    void onP2PBroadcastReceived(P2PBroadcastReceivedEvent event) {
        Debug.d "onP2PBroadcastReceived"

        switch (event.intent.action) {
            case WIFI_P2P_STATE_CHANGED_ACTION:
                Debug.d "WIFI_P2P_STATE_CHANGED_ACTION"
                break
            case WIFI_P2P_PEERS_CHANGED_ACTION:
                handler.postDelayed({
                        manager.requestPeers channel, { WifiP2pDeviceList peerList ->
                            // Out with the old, in with the new.
//                            if (peerList.getDeviceList().size() != peers.size()) {
                            peers.clear()
                            peers.addAll peerList.deviceList

                            adapter.notifyDataSetChanged()
//                            }

                            Debug.d "Peers:"
                            peers.each {
                                Debug.d "deviceAddress: ${it.deviceAddress}; deviceName: ${it.deviceName}"
                            }
                        }
                }, 1000)
                Debug.d "WIFI_P2P_PEERS_CHANGED_ACTION"
                break
            case WIFI_P2P_CONNECTION_CHANGED_ACTION:
                def networkInfo = event.intent.getParcelableExtra(EXTRA_NETWORK_INFO) as NetworkInfo

                if (networkInfo.connected) {

                    // We are connected with the other device, request connection
                    // info to find group owner IP

                    manager.requestConnectionInfo channel, { WifiP2pInfo info ->
                        Debug.d "onConnectionInfoAvailable: ${info}"
//                            // InetAddress from WifiP2pInfo struct.
//                            InetAddress groupOwnerAddress = info.groupOwnerAddress;
//
//                            // After the group negotiation, we can determine the group owner.
//                            if (info.groupFormed && info.isGroupOwner) {
//                                // Do whatever tasks are specific to the group owner.
//                                // One common case is creating a webSocketServer thread and accepting
//                                // incoming connections.
//                            } else if (info.groupFormed) {
//                                // The other device acts as the client. In this case,
//                                // you'll want to create a client thread that connects to the group
//                                // owner.
//
//                                gameWebSocketClient = app.getWebSocketMessageClient(
//                                        URI.create("ws://" + groupOwnerAddress.getHostAddress() + ":4807")
//                                );
//                                gameWebSocketClient.connect();
//                            }
                    }
                }
                Debug.d "WIFI_P2P_CONNECTION_CHANGED_ACTION"
                break
            case WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                Debug.d "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION"
                break
        }
    }

    @OnClick(R.id.btn_discover)
    void discoverClicked() {
        discoverService()
    }

    @OnClick(R.id.btn_register)
    void registerClicked() {
        startRegistration()
    }

    private void connect(int index) {
        // Picking the first device found on the network.
        WifiP2pDevice device = peers[index]

        WifiP2pConfig config = new WifiP2pConfig()
        config.deviceAddress = device.deviceAddress
        config.wps.setup = WpsInfo.PBC

        manager.connect channel, config, [
                onSuccess: { Debug.d("MANAGER: joined") },
                onFailure: { int reason -> Toast.makeText(activity, "Connect failed. (${reason})", Toast.LENGTH_SHORT).show() }
        ] as ActionListener
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
    void onSocketOpened(SocketOpenedEvent event) {
        startStreamActivity()
    }


}
