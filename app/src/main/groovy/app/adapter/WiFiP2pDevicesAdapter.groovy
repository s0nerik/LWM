package app.adapter
import android.net.wifi.p2p.WifiP2pDevice
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import app.Injector
import app.R
import app.adapter.view_holders.WifiP2pDeviceViewHolder
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
public class WiFiP2pDevicesAdapter extends RecyclerView.Adapter<WifiP2pDeviceViewHolder> {

    @Inject
    @PackageScope
    LayoutInflater layoutInflater

    @Inject
    @PackageScope
    Bus bus

    private final List<WifiP2pDevice> devices

    WiFiP2pDevicesAdapter(List<WifiP2pDevice> devices) {
        this.devices = devices
        Injector.inject this
    }

    @Override
    public WifiP2pDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WifiP2pDeviceViewHolder(layoutInflater.inflate(R.layout.item_join_game, null), devices)
    }

    @Override
    public void onBindViewHolder(WifiP2pDeviceViewHolder holder, int position) {
        holder.title.text = devices[position].deviceName
    }

    @Override
    public int getItemCount() {
        return devices.size()
    }

}
