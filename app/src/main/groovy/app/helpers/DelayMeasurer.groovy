package app.helpers

import groovy.transform.CompileStatic
import org.apache.commons.lang3.time.StopWatch

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