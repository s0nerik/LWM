package app.adapters.base

import android.support.v7.widget.RecyclerView
import eu.davidea.flexibleadapter.items.AbstractExpandableItem
import eu.davidea.flexibleadapter.items.IFlexible
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@TupleConstructor
@CompileStatic
class MediaStoreExpandableItem<VH extends RecyclerView.ViewHolder, S extends IFlexible> extends AbstractExpandableItem<VH, S> {
    long id

    @Override
    boolean equals(Object o) {
        if (o instanceof MediaStoreExpandableItem) {
            return id == (o as MediaStoreExpandableItem).id
        }
        return false
    }
}