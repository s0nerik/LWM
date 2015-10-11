package app.helper

import groovy.transform.CompileStatic
import org.apache.commons.collections4.queue.CircularFifoQueue
import org.apache.commons.lang3.time.StopWatch
import ru.noties.debug.Debug

@CompileStatic
class DelayMeasurer {

    private AveragingCollection<Long> delays = new AveragingCollection<Long>(10)

    private StopWatch stopwatch = new StopWatch()

    void start() {
        if (stopwatch.started) return

        stopwatch.start()
    }

    void stop() {
        if (!stopwatch.started) return

        stopwatch.stop()
        delays << stopwatch.time
        stopwatch.reset()
    }

    void cancel() {
        stopwatch.reset()
    }

    public long getAverage() {
        return delays.average
    }

}