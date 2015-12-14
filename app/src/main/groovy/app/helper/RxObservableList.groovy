package app.helper

import groovy.transform.CompileStatic
import rx.Observable
import rx.subjects.PublishSubject

@CompileStatic
class RxObservableList<T> {

    protected final List<T> list
    protected final PublishSubject<T> onAdd

    RxObservableList(List<T> list) {
        this.list = list
        onAdd = PublishSubject.create()
    }

    void add(T value) {
        list.add(value)
        onAdd.onNext(value)
    }

    Observable<T> getObservable() {
        onAdd
    }
}