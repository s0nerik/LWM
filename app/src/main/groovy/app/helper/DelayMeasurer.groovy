package app.helper

import groovy.transform.CompileStatic
import org.apache.commons.collections4.queue.CircularFifoQueue
import org.apache.commons.lang3.time.StopWatch
import ru.noties.debug.Debug

@CompileStatic
class DelayMeasurer {

    private CircularFifoQueue<Long> delays = new CircularFifoQueue<Long>(10)

    private StopWatch stopwatch = new StopWatch()

    long average = 0

    void start() {
        if (stopwatch.started) return

        stopwatch.start()
    }

    void stop() {
        if (!stopwatch.started) return

        stopwatch.stop()
        delays << stopwatch.time
        updateAverage()
        stopwatch.reset()
    }

    void cancel() {
        stopwatch.reset()
    }

    private void updateAverage() {
        average = (delays.sum() as long) / delays.size() as long

        Debug.d "New delay average: ${average}"
    }

}