package app.adapters.stations

import android.view.View
import android.widget.TextView
import app.App
import app.R
import app.helpers.StationsExplorer
import app.models.Station
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.viewholders.FlexibleViewHolder
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
class StationViewHolder extends FlexibleViewHolder {

    @Inject
    protected StationsExplorer explorer

    @InjectView(R.id.title)
    TextView title

    @InjectView(R.id.subtitle)
    TextView subtitle

    Station station

    StationViewHolder(View view, FlexibleAdapter adapter) {
        super(view, adapter)
        App.get().inject this
        BetterKnife.inject this, itemView
    }

    void setStation(Station station) {
        this.station = station

        title.text = station.info.name
    }

    @OnClick(R.id.item)
    void onItemClicked(View item) {
        explorer.connect(station.device)
    }

}