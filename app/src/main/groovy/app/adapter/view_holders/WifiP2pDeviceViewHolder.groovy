package app.adapter.view_holders

import android.net.wifi.p2p.WifiP2pDevice
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import app.Injector
import app.R
import app.events.ui.WifiP2pDeviceSelectedEvent
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
class WifiP2pDeviceViewHolder extends RecyclerView.ViewHolder {

    @Inject
    @PackageScope
    Bus bus

    @InjectView(R.id.title)
    TextView title

    @InjectView(R.id.subtitle)
    TextView subtitle

    private final List<WifiP2pDevice> devices

    WifiP2pDeviceViewHolder(View itemView, List<WifiP2pDevice> devices) {
        super(itemView)
        this.devices = devices
        Injector.inject this
        BetterKnife.inject this, itemView
    }

    @OnClick(R.id.item)
    void onItemClicked(View item) {
        bus.post new WifiP2pDeviceSelectedEvent(devices[adapterPosition])
    }

}