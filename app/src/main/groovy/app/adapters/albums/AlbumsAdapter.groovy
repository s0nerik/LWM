package app.adapters.albums

import android.support.annotation.NonNull
import eu.davidea.flexibleadapter.FlexibleAdapter
import groovy.transform.CompileStatic

@CompileStatic
class AlbumsAdapter extends FlexibleAdapter<AlbumItem> {
    AlbumsAdapter(@NonNull List<AlbumItem> items) {
        super(items)
    }
}
