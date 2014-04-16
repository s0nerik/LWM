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
import com.lwm.app.ui.activity.RemotePlaybackActivity;

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
        App.getMusicService().getStreamPlayer().playFromCurrentPosition();
        Intent intent = new Intent(getActivity(), RemotePlaybackActivity.class);
        getActivity().startActivity(intent);
    }

    private class StationConnectionTask extends AsyncTask<String, Void, Void>{
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
            if (wifiManager != null) {
                if(wifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLED){
                    WifiInfo info = wifiManager.getConnectionInfo();
                    if (info != null || !ap.equals(info.getSSID())) {
                        // Device is connected to different AP or not connected at all
                        Connectivity.connectToOpenAP(getActivity(), ap);
                    }
                }else{
                    // Wifi is disabled, so let's turn it on and connect
                    wifiManager.setWifiEnabled(true);

                    // Wait until it Wifi is enabled
                    try {
                        while(!wifiManager.isWifiEnabled()){
                            Thread.sleep(500);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Connectivity.connectToOpenAP(getActivity(), ap);

                    // Wait until Wifi is really connected
                    try {
                        while(!Connectivity.isConnectedWifi(getActivity())){
                            Thread.sleep(500);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            while(!isWifiNetworkAvailable()){
                try {
                    Thread.sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
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
