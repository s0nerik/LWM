package app.adapters.stations

import android.view.LayoutInflater
import android.view.ViewGroup
import app.R
import app.models.Station
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@TupleConstructor
@CompileStatic
class StationItem extends AbstractFlexibleItem<StationViewHolder> {
    Station station

    @Override
    int getLayoutRes() {
        return R.layout.item_stations
    }

    @Override
    StationViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new StationViewHolder(inflater.inflate(layoutRes, parent, false), adapter)
    }

    @Override
    void bindViewHolder(FlexibleAdapter adapter, StationViewHolder holder, int position, List payloads) {
        holder.station = station
    }

    @Override
    boolean equals(Object o) {
        if (o instanceof StationItem)
            return station.device.deviceAddress == o?.station?.device?.deviceAddress
        return false
    }
}