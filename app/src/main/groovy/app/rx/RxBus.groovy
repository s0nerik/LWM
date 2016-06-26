package app.rx

import groovy.transform.CompileStatic
import rx.Observable
import rx.subjects.PublishSubject

/**
 * Created by Alex on 5/26/2016.
 */
@CompileStatic
class RxBus {
    private static PublishSubject<Object> subject = PublishSubject.create()

    static <T> Observable<T> on(Class<T> type) {
        subject.filter { it != null && type.isAssignableFrom(it.class) }.cast(type)
    }

    static void post(Object event) {
        subject.onNext(event)
    }
}