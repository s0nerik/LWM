package app.helpers

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.apache.commons.collections4.queue.CircularFifoQueue

@InheritConstructors
@CompileStatic
class AveragingCollection<T extends Number> extends CircularFifoQueue<T> {

    T average

    private Closure<T> itemGetter

    AveragingCollection(int size, Closure<T> itemGetter) {
        super(size)
        this.itemGetter = itemGetter
    }

    AveragingCollection(Closure<T> itemGetter) {
        super()
        this.itemGetter = itemGetter
    }

    @Override
    boolean add(T element) {
        def result = super.add(itemGetter ? itemGetter(element) : element)
        average = sum { T it -> it.intdiv size() } as T
        return result
    }
}