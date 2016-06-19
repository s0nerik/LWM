package app.adapters.songs

import android.support.annotation.NonNull
import eu.davidea.flexibleadapter.FlexibleAdapter
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
class SongsListAdapter extends FlexibleAdapter<SongItem> {

    Closure<String> bubbleTextProvider

    SongsListAdapter(@NonNull List<SongItem> items) {
        super(items)
    }

    @Override
    String onCreateBubbleText(int position) {
        return bubbleTextProvider(getItem(position))
    }

    void setBubbleTextProvider(@ClosureParams(value=FromString, options=["app.adapter.songs.SongItem"]) Closure<String> bubbleTextProvider) {
        this.bubbleTextProvider = bubbleTextProvider
    }
}
