package app.adapter

import android.support.annotation.NonNull
import app.adapter.items.SongItem
import eu.davidea.flexibleadapter.FlexibleAdapter
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
class SongsListAdapter extends FlexibleAdapter<SongItem> {

    SongsListAdapter(@NonNull List<SongItem> items) {
        super(items)
    }

}
