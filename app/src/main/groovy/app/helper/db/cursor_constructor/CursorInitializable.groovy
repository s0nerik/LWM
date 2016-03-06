package app.helper.db.cursor_constructor
import android.database.Cursor
import groovy.transform.CompileStatic

@CompileStatic
interface CursorInitializable {
    void initialize(Cursor cursor, Map<String, Integer> indices)
}