package app.helper
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

@CompileStatic
class GroovyLongSparseArray<T> extends SerializableLongSparseArray<T> {

    List<T> filter(@ClosureParams(value = FromString, options = ['T']) Closure<Boolean> predicate) {
        def filtered = new ArrayList<T>()
        for(int i = 0; i < size(); i++) {
            def item = valueAt(i)
            if (predicate(item)) filtered << item
        }

        return filtered
    }

    T first(@ClosureParams(value = FromString, options = ['T']) Closure<Boolean> predicate) {
        for(int i = 0; i < size(); i++) {
            def item = valueAt(i)
            if (predicate(item)) return item
        }
        return null
    }

    List<T> asList() {
        def list = new ArrayList<T>()
        for(int i = 0; i < size(); i++) {
            list << valueAt(i)
        }

        return list
    }

}