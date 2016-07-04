package app.ui.fragment

import android.support.annotation.IdRes
import android.support.annotation.NonNull
import android.support.v7.widget.RecyclerView
import app.R
import groovy.transform.CompileStatic

@CompileStatic
trait SortableFragment {

    @IdRes
    int sortActionId = defaultSortActionId
    boolean orderAscending = true

    @IdRes
    abstract int getSortMenuId()
    @IdRes
    abstract int getDefaultSortActionId()
    @NonNull
    abstract List<?> getSortableList()

    abstract Map<Integer, Closure> getSorters()
    abstract RecyclerView.Adapter getAdapter()

    @IdRes
    int getSortActionId() {
        sortActionId
    }

    @IdRes
    void setSortActionId(int i) {
        sortActionId = i
    }

    boolean getOrderAscending() {
        orderAscending
    }

    void setOrderAscending(boolean b) {
        orderAscending = b
    }

    @IdRes
    int getSortIconId() {
        return orderAscending ? R.drawable.sort_ascending : R.drawable.sort_descending
    }

    void sortItems() {
        sortableList.sort true, sorters[sortActionId]
        if (!orderAscending)
            sortableList.reverse true

        adapter.notifyDataSetChanged()

//        artists.sort true, SorterProviders.ARTISTS[sortActionId]
//        if (!orderAscending)
//            artists.reverse true
//
//        unfilteredArtists = new ArrayList<>(artists)
//        adapter.notifyDataSetChanged()
    }
}