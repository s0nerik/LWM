package com.lwm.app.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.astuetz.PagerSlidingTabStrip;
import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.PlayersAroundPagerAdapter;
import com.lwm.app.events.wifi.WifiScanResultsAvailableEvent;
import com.lwm.app.events.wifi.WifiStateChangedEvent;
import com.lwm.app.helper.wifi.WifiAP;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.ui.base.DaggerFragment;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class StationsAroundFragment extends DaggerFragment {

    @Inject
    Bus bus;

    @Inject
    WifiManager wifiManager;

    @Inject
    WifiAP wifiAP;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.tabs)
    PagerSlidingTabStrip mTabs;
    @InjectView(R.id.pager)
    ViewPager mPager;
    @InjectView(R.id.no_wifi_frame)
    LinearLayout mNoWifiFrame;

    private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            switch (i.getAction()) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    Log.d(App.TAG, "SCAN_RESULTS_AVAILABLE_ACTION");
                    bus.post(new WifiScanResultsAvailableEvent(wifiManager.getScanResults()));
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    Log.d(App.TAG, "WIFI_STATE_CHANGED_ACTION");
                    bus.post(new WifiStateChangedEvent(wifiManager));
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().stopService(new Intent(getActivity(), LocalPlayerService.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stations_around, container, false);
        ButterKnife.inject(this, v);

        mToolbar.setTitle(getString(R.string.stations_around));

        mPager.setAdapter(new PlayersAroundPagerAdapter(getChildFragmentManager()));

        mTabs.setViewPager(mPager);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(onBroadcast, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        getActivity().registerReceiver(onBroadcast, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        toggleNoWifiFrame();
        bus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(onBroadcast);
        bus.unregister(this);
    }

    private void toggleNoWifiFrame() {
        if (!wifiManager.isWifiEnabled()) {
            mPager.setVisibility(View.GONE);
            mNoWifiFrame.setVisibility(View.VISIBLE);
        } else {
            mPager.setVisibility(View.VISIBLE);
            mNoWifiFrame.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void onWifiStateChanged(WifiStateChangedEvent event) {
        toggleNoWifiFrame();
    }

    @Produce
    public Toolbar produceToolbar() {
        return mToolbar;
    }

    @OnClick(R.id.no_wifi_frame)
    public void onNoWifiClicked() {
        if (wifiAP.isEnabled()) {
            wifiAP.toggleWiFiAP();
        } else {
            wifiManager.setWifiEnabled(true);
        }
    }

}
