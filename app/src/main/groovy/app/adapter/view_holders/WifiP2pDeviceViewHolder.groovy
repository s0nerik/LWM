package app.adapter.view_holders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import app.Injector
import app.R
import app.helper.StationsExplorer
import app.model.Station
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

    @Inject
    @PackageScope
    StationsExplorer explorer

    @InjectView(R.id.title)
    TextView title

    @InjectView(R.id.subtitle)
    TextView subtitle

    private final List<Station> stations

    WifiP2pDeviceViewHolder(View itemView, List<Station> stations) {
        super(itemView)
        this.stations = stations
        Injector.inject this
        BetterKnife.inject this, itemView
    }

    @OnClick(R.id.item)
    void onItemClicked(View item) {
        explorer.connect(stations[adapterPosition].device)
    }

}