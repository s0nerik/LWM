package app.helper.db
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import app.Daggered
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
abstract class CursorGetter extends Daggered {

    @Inject
    @PackageScope
    ContentResolver contentResolver

    abstract Uri getContentUri()
    abstract List<String> getProjection()
    abstract String getSelection()
    abstract List<String> getSelectionArgs()
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
                selection,
                selectionArgs as String[],
                sortOrder.toString()
        )
    }
}
