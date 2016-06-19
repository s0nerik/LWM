package app.adapters.stations

import android.support.annotation.NonNull
import eu.davidea.flexibleadapter.FlexibleAdapter
import groovy.transform.CompileStatic

@CompileStatic
class StationsAdapter extends FlexibleAdapter<StationItem> {
    StationsAdapter(@NonNull List<StationItem> items) {
        super(items)
    }
}
