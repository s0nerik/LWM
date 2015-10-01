package app.helper

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import ru.noties.debug.Debug
import rx.Observable
import rx.Subscription

import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

class PingResult {
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

    private final Closure pingPerformer

    private Deque<PingResult> pings = new LinkedBlockingDeque<PingResult>(DEQUE_LIMIT)

    int average = 0

    Subscription pingWorker

    PingMeasurer(Closure pingPerformer) {
        this.pingPerformer = pingPerformer
    }

    void start() {
        pingWorker = Observable.interval(PERIOD, TimeUnit.MILLISECONDS)
                               .doOnNext { pingPerformer() }
                               .subscribe()
    }

    void pongReceived(PingResult result) {
        pings << result
        updateAverage()
    }

    void stop() {
        pingWorker.unsubscribe()
    }

    private void updateAverage() {
        int ping = 0
        for (int i = 1; i < pings.size(); i++) {
            ping += (pings[i].pongReceivedTime - pings[i-1].pongReceivedTime - PERIOD) / pings.size() as int
        }
        average = ping

        Debug.d "New average ping: ${average}"
    }
}