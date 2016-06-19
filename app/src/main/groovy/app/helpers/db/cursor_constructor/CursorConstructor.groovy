package app.helpers.db.cursor_constructor
import app.helpers.db.CursorGetter
import groovy.transform.CompileStatic
import rx.Observable
import rx.Subscriber

@CompileStatic
class CursorConstructor {

    static <T extends CursorInitializable> Observable<T> fromCursorGetter(Class<T> clazz, CursorGetter cursorGetter, Closure<Boolean> check = { true }) {
        Observable.create({ Subscriber<T> subscriber ->
            def cursor = cursorGetter.cursor

            if (cursor) {
                if (cursor.moveToFirst()) {
                    def indices = cursorGetter.projectionIndices()

                    def produceItem = {
                        T item = clazz.newInstance() as T
                        item.initialize(cursor, indices)
                        if (check(item)) subscriber.onNext item
                    }

                    produceItem()

                    while (cursor.moveToNext()) {
                        produceItem()
                    }
                }
                cursor.close()
            }
            subscriber.onCompleted()
        } as Observable.OnSubscribe<T>)
    }

}