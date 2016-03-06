package app.ui.fragment

import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import app.R
import app.Utils
import app.adapter.PlayersAroundPagerAdapter
import app.events.wifi.WifiStateChangedEvent
import app.service.LocalPlayerService
import app.ui.base.DaggerFragment
import com.astuetz.PagerSlidingTabStrip
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnClick
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = R.layout.fragment_stations_around, injectAllViews = true)
class StationsAroundFragment extends DaggerFragment {

    @Inject
    @PackageScope
    Bus bus

    @Inject
    @PackageScope
    WifiManager wifiManager

    @Inject
    @PackageScope
    Utils utils

    Toolbar toolbar
    PagerSlidingTabStrip tabs
    ViewPager pager
    View noWifiFrame

//    private BroadcastReceiver onBroadcast = { Context context, Intent i ->
//        switch (i.action) {
//            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
//                Debug.d "SCAN_RESULTS_AVAILABLE_ACTION"
//                bus.post new WifiScanResultsAvailableEvent(wifiManager.scanResults)
//                break
//            case WifiManager.WIFI_STATE_CHANGED_ACTION:
//                Debug.d "WIFI_STATE_CHANGED_ACTION"
//                bus.post new WifiStateChangedEvent(wifiManager)
//                break
//        }
//    } as BroadcastReceiver

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate savedInstanceState
        activity.stopService new Intent(activity, LocalPlayerService)
    }

    @Override
    void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated view, savedInstanceState
        toolbar.title = R.string.stations_around
        pager.adapter = new PlayersAroundPagerAdapter(childFragmentManager)
        tabs.viewPager = pager
    }

    @Override
    void onResume() {
        super.onResume()
//        activity.registerReceiver onBroadcast, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
//        activity.registerReceiver onBroadcast, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        bus.register this
        toggleNoWifiFrame()
    }

    @Override
    void onPause() {
        super.onPause()
//        activity.unregisterReceiver onBroadcast
        bus.unregister this
    }

    private void toggleNoWifiFrame() {
        if (!wifiManager.wifiEnabled) {
            pager.visibility = View.GONE
            noWifiFrame.visibility = View.VISIBLE
        } else {
            pager.visibility = View.VISIBLE
            noWifiFrame.visibility = View.GONE
        }
    }

    @Subscribe
    void onWifiStateChanged(WifiStateChangedEvent event) {
        toggleNoWifiFrame()
    }

    @Produce
    Toolbar produceToolbar() {
        return toolbar
    }

    @OnClick(R.id.no_wifi_frame)
    void onNoWifiClicked() {
        wifiManager.wifiEnabled = true
//        if (wifiAP.enabled) {
//            wifiAP.toggleWiFiAP()
//        } else {
//            wifiManager.wifiEnabled = true
//        }
    }

}
