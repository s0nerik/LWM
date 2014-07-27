package com.lwm.app.ui.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.lib.Connectivity;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.server.StreamServer;
import com.lwm.app.ui.activity.RemotePlaybackActivity;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.List;

public class PlayersAroundFragment extends ListFragment {

    private List<String> ssids;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(savedInstanceState == null){
            WifiManager wm = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            wm.startScan();
            Log.d(App.TAG, "wm.startScan()");
        }

        return inflater.inflate(R.layout.fragment_players_around, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String ap = ssids.get(position) + WifiAP.AP_NAME_POSTFIX;

        new StationConnectionTask().execute(ap);

    }

    private boolean isWifiNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void setSSIDs(List<String> ssids){
        Log.d(App.TAG, "setSSIDs()");
        this.ssids = ssids;
        setListAdapter(new ArrayAdapter(getActivity(), R.layout.list_item_players_around, ssids));
    }

    protected void startStreamPlayback(){
        App.getStreamPlayerService().attachToStation();
        Intent intent = new Intent(getActivity(), RemotePlaybackActivity.class);
        getActivity().startActivity(intent);
    }

    private class StationConnectionTask extends AsyncTask<String, Void, Void> {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPostPing = new HttpPost(StreamServer.SERVER_ADDRESS);

        ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getString(R.string.connecting_to_the_station));
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... aps) {
            String ap = aps[0];
            WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null && wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                WifiInfo info = wifiManager.getConnectionInfo();
                if (info == null || !ap.equals(info.getSSID())) {
                    // Device is connected to different AP or not connected at all
                    Connectivity.connectToOpenAP(getActivity(), ap);
                    while(!isWifiNetworkAvailable()){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    while(true) {
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
            startStreamPlayback();
        }
    }

}
