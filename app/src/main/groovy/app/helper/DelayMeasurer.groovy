package app.helper

import groovy.transform.CompileStatic
import org.apache.commons.lang3.time.StopWatch

import java.util.concurrent.LinkedBlockingDeque

@CompileStatic
class DelayMeasurer {

    private Deque<Long> delays = new LinkedBlockingDeque<Long>(10)

    private StopWatch stopwatch = new StopWatch()

    long average = 0

    void start() {
        stopwatch.start()
    }

    void stop() {
        if (!stopwatch.started) return

        stopwatch.stop()
        delays.offerLast(stopwatch.time)
        updateAverage()
        stopwatch.reset()
    }

    void cancel() {
        stopwatch.reset()
    }

    private void updateAverage() {
        average = (delays.sum() as long) / delays.size() as long
    }

}