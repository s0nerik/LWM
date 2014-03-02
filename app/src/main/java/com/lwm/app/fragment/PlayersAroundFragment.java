package com.lwm.app.fragment;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.lwm.app.App;
import com.lwm.app.R;

import java.util.List;

public class PlayersAroundFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(savedInstanceState == null){
            WifiManager wm = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            wm.startScan();
            Log.d(App.TAG, "wm.startScan()");
        }
//        new AsyncTask<Void, Void, Void>(){
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//                wm.startScan();
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                ArrayList<String> ssids = new ArrayList<>();
//                for(ScanResult result:wm.getScanResults()){
//                    ssids.add(result.SSID);
//                }
//                setListAdapter(new ArrayAdapter(getActivity(), R.layout.list_item_players_around, ssids));
//            }
//        }.execute();

        return inflater.inflate(R.layout.fragment_players_around, container, false);
    }

    public void setSSIDs(List<String> ssids){
        Log.d(App.TAG, "setSSIDs()");
        setListAdapter(new ArrayAdapter(getActivity(), R.layout.list_item_players_around, ssids));
    }

}
