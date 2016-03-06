package app.helper

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.apache.commons.collections4.queue.CircularFifoQueue
import org.apache.commons.lang3.time.StopWatch
import ru.noties.debug.Debug

@CompileStatic
class DelayMeasurer<T extends Number> extends AveragingCollection<T> {

    private StopWatch stopwatch = new StopWatch()

    DelayMeasurer(int size) {
        super(size)
    }

    void start() {
        if (stopwatch.started) return

        stopwatch.start()
    }

    void stop() {
        if (!stopwatch.started) return

        stopwatch.stop()
        add stopwatch.time as T
        stopwatch.reset()
    }

    void cancel() {
        stopwatch.reset()
    }

}