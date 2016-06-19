package app.adapters.artists

import android.support.annotation.NonNull
import eu.davidea.flexibleadapter.FlexibleAdapter
import groovy.transform.CompileStatic

@CompileStatic
class ArtistsAdapter extends FlexibleAdapter<ArtistItem> {
    ArtistsAdapter(@NonNull List<ArtistItem> items) {
        super(items)
    }
}
