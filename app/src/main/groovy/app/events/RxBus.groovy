package app.events

import groovy.transform.CompileStatic
import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject
import rx.subjects.Subject

@CompileStatic
class RxBus {
    private final Subject<Object, Object> bus = new SerializedSubject(PublishSubject.create())

    void send(Object o) {
        bus.onNext(o)
    }

    Observable toObserverable() {
        return bus
    }
}