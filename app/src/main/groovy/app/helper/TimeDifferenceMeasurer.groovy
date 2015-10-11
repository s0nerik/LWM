package app.helper

import groovy.transform.CompileStatic

@CompileStatic
class TimeDifferenceMeasurer {

    private AveragingCollection<Long> differences

    TimeDifferenceMeasurer() {
        differences = new AveragingCollection<>({ long it -> System.currentTimeMillis() - it })
    }

    void add(long item) {
        differences << item
    }

    long getDifference() { differences.average }

    long toLocalTime(long time) {
        time + differences.average
    }

}