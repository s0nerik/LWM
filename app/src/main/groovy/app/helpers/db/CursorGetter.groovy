package app.helpers.db

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
abstract class CursorGetter {

    @Inject
    protected ContentResolver contentResolver

    abstract Uri getContentUri()
    abstract List<String> getProjection()
    abstract List<String> getSelection()
    abstract SortOrder getSortOrder()

    Map<String, Integer> projectionIndices() {
        def indices = [:]

        for (int i = 0; i < projection.size(); i++) {
            indices[projection[i]] = i
        }

        return indices
    }

    Cursor getCursor() {
        contentResolver.query(
                contentUri,
                projection as String[],
                makeSelection() ?: null,
                null,
                sortOrder.toString()
        )
    }

    private String makeSelection() {
        selection?.join " AND "
    }
}
