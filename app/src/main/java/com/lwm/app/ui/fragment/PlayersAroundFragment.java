package com.lwm.app.ui.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.StationsAdapter;
import com.lwm.app.events.client.SocketOpenedEvent;
import com.lwm.app.events.server.StartWebSocketClientEvent;
import com.lwm.app.events.wifi.WifiScanResultsAvailableEvent;
import com.lwm.app.lib.Connectivity;
import com.lwm.app.server.StreamServer;
import com.lwm.app.ui.activity.RemotePlaybackActivity;
import com.squareup.otto.Subscribe;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class PlayersAroundFragment extends Fragment {

    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.emptyView)
    LinearLayout mEmptyView;
    @InjectView(R.id.listView)
    ListView mListView;
    @InjectView(R.id.refreshLayout)
    SwipeRefreshLayout mRefreshLayout;

    private List<ScanResult> scanResults;
    private StationsAdapter stationsAdapter;
    private WifiManager wifiManager;

    private boolean isRefreshing = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_players_around, container, false);
        ButterKnife.inject(this, v);

        mRefreshLayout.setColorScheme(
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

    @OnItemClick(R.id.listView)
    void onStationClicked(int position) {
        String ap = stationsAdapter.getItem(position).SSID;
        new StationConnectionTask().execute(ap);
    }

    @OnClick(R.id.btnRefresh)
    void onRefreshStations(View v) {
        startScanningStations(false);
    }

    private boolean isWifiNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Subscribe
    public void onScanResultsAvailable(WifiScanResultsAvailableEvent event) {
        Log.d(App.TAG, "setScanResults()");
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
                    Connectivity.connectToStation(getActivity(), ap);
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
            App.getBus().post(new StartWebSocketClientEvent());
//            startStreamActivity();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        App.getBus().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        App.getBus().unregister(this);
    }

    @Subscribe
    public void onSocketOpened(SocketOpenedEvent event) {
        startStreamActivity();
    }

}
