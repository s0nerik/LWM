package app.ui.fragment

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import app.R
import app.adapter.StationsAdapter
import app.events.client.SocketOpenedEvent
import app.events.server.ShouldStartWebSocketClientEvent
import app.events.wifi.WifiScanResultsAvailableEvent
import app.helper.wifi.WifiAP
import app.helper.wifi.WifiUtils
import app.server.StreamServer
import app.service.StreamPlayerService
import app.ui.activity.RemotePlaybackActivity
import app.ui.base.DaggerFragment
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.github.s0nerik.betterknife.annotations.OnItemClick
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import ru.noties.debug.Debug

import javax.inject.Inject
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class FindStationsFragment extends DaggerFragment {

    @Inject
    Bus bus;

    @Inject
    WifiUtils wifiUtils;

    @Inject
    WifiManager wifiManager;

    @InjectView(R.id.twoWayView)
    ListView mListView;
    @InjectView(R.id.refreshLayout)
    SwipeRefreshLayout mRefreshLayout;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.btnRefresh)
    Button mBtnRefresh;
    @InjectView(R.id.emptyView)
    LinearLayout mEmptyView;

    private List<ScanResult> scanResults;
    private StationsAdapter stationsAdapter;

    private boolean isRefreshing = false;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.page_stations_around, container, false);

        mRefreshLayout.setColorSchemeResources(
                R.color.pull_to_refresh_1,
                R.color.pull_to_refresh_2,
                R.color.pull_to_refresh_3,
                R.color.pull_to_refresh_4
        );

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startScanningStations(true);
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            startScanningStations(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @OnItemClick(R.id.twoWayView)
    void onStationClicked(int position) {
        String ap = stationsAdapter.getItem(position).SSID;
        new StationConnectionTask().execute(ap);
    }

    @OnClick(R.id.btnRefresh)
    void onRefreshStations(View v) {
        startScanningStations(false);
    }

    private boolean isWifiNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Subscribe
    public void onScanResultsAvailable(WifiScanResultsAvailableEvent event) {
        Debug.d("setScanResults()");
        scanResults = event.getScanResults();

        stationsAdapter = new StationsAdapter(getActivity(), scanResults);

        if (isRefreshing) {
            mRefreshLayout.setRefreshing(false);
            isRefreshing = false;
        }

        mProgressBar.setVisibility(View.GONE);
        if (stationsAdapter.getCount() > 0) {
            mListView.setVisibility(View.VISIBLE);
            mListView.setAdapter(stationsAdapter);
        } else {
            mListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    // For debug only
    private ScanResult createFakeScanResult() {
        try {
            Constructor<ScanResult> ctor = ScanResult.class.getDeclaredConstructor(null);
            ctor.setAccessible(true);
            ScanResult sr = ctor.newInstance(null);
            sr.BSSID = "foo";
            sr.SSID = String.format(WifiAP.AP_NAME_FORMAT, "Ololo", "#000000");
            sr.level = 3;
            return sr;
        } catch (SecurityException | InvocationTargetException | NoSuchMethodException | java.lang.InstantiationException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    protected void startStreamActivity() {
        Intent intent = new Intent(getActivity(), RemotePlaybackActivity.class);
        getActivity().startActivity(intent);
    }

    private void startScanningStations(boolean isPulled) {
        wifiManager.startScan();
        if (isPulled) {
            mProgressBar.setVisibility(View.GONE);
            isRefreshing = true;
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
        mEmptyView.setVisibility(View.GONE);
    }

    private class StationConnectionTask extends AsyncTask<String, Void, Void> {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPostPing = new HttpPost(StreamServer.Url.SERVER_ADDRESS);

        ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getString(R.string.connecting_to_the_station));
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            String ap = params[0];
//            String pass = params[1];

            WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null && wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                WifiInfo info = wifiManager.getConnectionInfo();
                if (info == null || !ap.equals(info.getSSID())) {
                    // Device is connected to different AP or not connected at all
                    wifiUtils.connectToStation(ap);
                    while (!isWifiNetworkAvailable()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    while (true) {
                        try {
                            httpclient.execute(httpPostPing);
                            break;
                        } catch (IOException ignored) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            getActivity().startService(new Intent(getActivity(), StreamPlayerService.class));
            bus.post(new ShouldStartWebSocketClientEvent());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        bus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    @Subscribe
    public void onSocketOpened(SocketOpenedEvent event) {
        startStreamActivity();
    }


}
