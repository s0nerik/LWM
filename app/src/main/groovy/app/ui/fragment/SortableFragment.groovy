package app.ui.fragment

import android.support.annotation.IdRes
import groovy.transform.CompileStatic

@CompileStatic
interface SortableFragment {
    @IdRes
    int getSortMenuId()
    @IdRes
    int getSortIconId()
    int getSortActionId()
    void setSortActionId(@IdRes int id)
    boolean isOrderAscending()
    void setOrderAscending(boolean value)
    void sortItems()
}