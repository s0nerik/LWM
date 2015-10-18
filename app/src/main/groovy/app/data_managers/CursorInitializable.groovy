package app.data_managers
import android.database.Cursor
import groovy.transform.CompileStatic

@CompileStatic
trait CursorInitializable {

    abstract void initialize(Cursor cursor, Map<String, Integer> indices)

}