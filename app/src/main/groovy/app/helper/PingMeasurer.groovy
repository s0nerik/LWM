package app.helper

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.apache.commons.collections4.queue.CircularFifoQueue
import ru.noties.debug.Debug
import rx.Observable
import rx.Subscription
import rx.functions.Action1

import java.util.concurrent.TimeUnit

class PingResult {
    long pingSentTime
    final long pingReceivedTime
    final long pongReceivedTime

    PingResult(long pingReceivedTime) {
        this.pingReceivedTime = pingReceivedTime
        pongReceivedTime = System.currentTimeMillis()
    }
}

@TupleConstructor(includes = ["pingPerformer"])
@CompileStatic
class PingMeasurer {

    private static final int PERIOD = 1000
    private static final int DEQUE_LIMIT = 10

    private final Action1<Long> pingPerformer

    private CircularFifoQueue<PingResult> pings = new CircularFifoQueue<PingResult>(DEQUE_LIMIT)

    private long pingSentTime

    int average = 0

    Subscription pingWorker

    PingMeasurer(Action1<Long> pingPerformer) {
        this.pingPerformer = pingPerformer
    }

    void start() {
        pingWorker = Observable.interval(PERIOD, TimeUnit.MILLISECONDS)
                               .subscribe {
                                    pingSentTime = System.currentTimeMillis()
                                    pingPerformer.call it
                               }
    }

    void pongReceived(PingResult result) {
        result.pingSentTime = pingSentTime
        pings << result
        updateAverage()
    }

    void stop() {
        pingWorker.unsubscribe()
    }

    private void updateAverage() {
//        int ping = 0
//        for (int i = 1; i < pings.size(); i++) {
//            ping += (pings[i].pongReceivedTime - pings[i-1].pongReceivedTime - PERIOD) / pings.size() as int
//        }
//        average = ping

        average = pings.sum { PingResult it -> (it.pongReceivedTime - it.pingSentTime) / 2 } as int

        Debug.d "New average ping: ${average}"
    }
}