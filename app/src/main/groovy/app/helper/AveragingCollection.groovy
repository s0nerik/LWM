package app.helper

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.apache.commons.collections4.queue.CircularFifoQueue

@InheritConstructors
@CompileStatic
class AveragingCollection<T extends Number> extends CircularFifoQueue<T> {

    T average

    @Override
    boolean add(T element) {
        def result = super.add(element)
        average = (sum() as T).intdiv(size()) as T
        return result
    }
}