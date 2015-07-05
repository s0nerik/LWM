package app.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import app.R
import app.adapter.PlayersAroundPagerAdapter
import app.events.wifi.WifiScanResultsAvailableEvent
import app.events.wifi.WifiStateChangedEvent
import app.helper.wifi.WifiAP
import app.service.LocalPlayerService
import app.ui.base.DaggerFragment
import com.astuetz.PagerSlidingTabStrip
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import ru.noties.debug.Debug

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
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
                    Debug.d("SCAN_RESULTS_AVAILABLE_ACTION");
                    bus.post(new WifiScanResultsAvailableEvent(wifiManager.getScanResults()));
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    Debug.d("WIFI_STATE_CHANGED_ACTION");
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

        mToolbar.setTitle(getString(R.string.stations_around));

        mPager.setAdapter(new PlayersAroundPagerAdapter(getChildFragmentManager()));

        mTabs.setViewPager(mPager);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

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
