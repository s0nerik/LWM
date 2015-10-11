package app.helper

import groovy.transform.CompileStatic

@CompileStatic
class TimeDifferenceMeasurer {

    private AveragingCollection<Long> times

    TimeDifferenceMeasurer() {
        times = new AveragingCollection<>({ long it -> System.currentTimeMillis() - it })
    }

    void add(long item) {
        times << item
    }

    long getDifference() { times.average }
}