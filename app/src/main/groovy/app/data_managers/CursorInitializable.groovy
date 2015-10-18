package app.data_managers
import android.database.Cursor
import groovy.transform.CompileStatic

@CompileStatic
interface CursorInitializable {
    void initialize(Cursor cursor, Map<String, Integer> indices)
}