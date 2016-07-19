package app.adapters.songs

import android.support.annotation.NonNull
import app.adapters.BubbleTextProvider
import eu.davidea.flexibleadapter.FlexibleAdapter
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

@CompileStatic
class SongsListAdapter extends FlexibleAdapter<SongItem> implements BubbleTextProvider {

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
