package app.ui.fragment

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import app.R
import app.adapters.stations.StationItem
import app.adapters.stations.StationsAdapter
import app.events.client.SocketOpenedEvent
import app.events.p2p.StationsListUpdatedEvent
import app.helpers.StationsExplorer
import app.ui.activity.RemotePlaybackActivity
import app.ui.base.OttoOnCreateFragment
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
@InjectLayout(R.layout.page_stations_around)
public class FindStationsFragment extends OttoOnCreateFragment {

    @Inject
    protected Bus bus

    @Inject
    protected ConnectivityManager connectivityManager

    @Inject
    protected Handler handler

    @Inject
    protected StationsExplorer explorer

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

    private StationsAdapter adapter
    private List<StationItem> stations = new ArrayList<>();

    private boolean isRefreshing = false

    @Override
    void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)

        adapter = new StationsAdapter(stations)

        recycler.layoutManager = new LinearLayoutManager(activity)
        recycler.adapter = adapter

        mRefreshLayout.setColorSchemeResources(
                R.color.pull_to_refresh_1,
                R.color.pull_to_refresh_2,
                R.color.pull_to_refresh_3,
                R.color.pull_to_refresh_4
        )
        mRefreshLayout.onRefreshListener = this.&onRefresh
    }

    @Override
    void onResume() {
        super.onResume()
        explorer.startStationsDiscovery()
    }

    @Override
    void onPause() {
        super.onPause()
        explorer.stopStationsDiscovery()
    }

    private boolean isWifiNetworkAvailable() {
        NetworkInfo activeNetworkInfo = connectivityManager.getNetworkInfo ConnectivityManager.TYPE_WIFI
        return activeNetworkInfo?.connected
    }

    protected void startStreamActivity() {
        Intent intent = new Intent(activity, RemotePlaybackActivity)
        activity.startActivity intent
    }

    private void onRefresh() {
        explorer.rediscover()
        mRefreshLayout.refreshing = false
    }

    @Subscribe
    void onStationsListUpdated(StationsListUpdatedEvent event) {
        stations.clear()
        stations.addAll event.stations.collect { new StationItem(it) }
        adapter.notifyDataSetChanged()
    }

    @OnClick(R.id.btn_discover)
    void discoverClicked() {
//        discoverStations()
    }

    @Subscribe
    void onSocketOpened(SocketOpenedEvent event) {
        startStreamActivity()
    }

}
