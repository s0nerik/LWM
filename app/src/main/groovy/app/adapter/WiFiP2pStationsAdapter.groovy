package app.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import app.Injector
import app.R
import app.adapter.view_holders.WifiP2pDeviceViewHolder
import app.model.Station
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
public class WiFiP2pStationsAdapter extends RecyclerView.Adapter<WifiP2pDeviceViewHolder> {

    @Inject
    @PackageScope
    LayoutInflater layoutInflater

    @Inject
    @PackageScope
    Bus bus

    private final List<Station> stations

    WiFiP2pStationsAdapter(List<Station> stations) {
        this.stations = stations
        Injector.inject this
    }

    @Override
    public WifiP2pDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WifiP2pDeviceViewHolder(layoutInflater.inflate(R.layout.item_join_game, null), stations)
    }

    @Override
    public void onBindViewHolder(WifiP2pDeviceViewHolder holder, int position) {
        holder.title.text = stations[position].info.name
    }

    @Override
    public int getItemCount() {
        return stations.size()
    }

}
