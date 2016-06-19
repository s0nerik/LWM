package app.adapters.base

import android.support.v7.widget.RecyclerView
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@TupleConstructor
@CompileStatic
class MediaStoreItem<VH extends RecyclerView.ViewHolder> extends AbstractFlexibleItem<VH> {
    long id

    @Override
    boolean equals(Object o) {
        if (o instanceof MediaStoreItem) {
            return id == (o as MediaStoreItem).id
        }
        return false
    }
}