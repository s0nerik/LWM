package app.helper

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import rx.Observable
import rx.Subscription
import rx.functions.Action1

import java.util.concurrent.TimeUnit

@CompileStatic
class PingMeasurer {

    private static final int WARMUP_COUNT = 10
    private static final int WARMUP_PERIOD = 250

    private static final int PERIOD = 1000
    private static final int PINGS_LIMIT = 10

    private final Action1<Long> pingPerformer

    private DelayMeasurer<Long> delayMeasurer = new DelayMeasurer<Long>(PINGS_LIMIT)

    Subscription pingWorker

    PingMeasurer(Action1<Long> pingPerformer) {
        this.pingPerformer = pingPerformer
    }

    void start() {
        pingWorker = Observable.interval(WARMUP_PERIOD, TimeUnit.MILLISECONDS)
                                .take(WARMUP_COUNT)
                                .concatWith(Observable.interval(PERIOD, TimeUnit.MILLISECONDS))
                                .subscribe {
                                    delayMeasurer.start()
                                    pingPerformer.call System.currentTimeMillis()
                                }

    }

    void pongReceived() {
        delayMeasurer.stop()
    }

    void stop() {
        pingWorker.unsubscribe()
    }

    long getAverage() {
        delayMeasurer.average
    }

}