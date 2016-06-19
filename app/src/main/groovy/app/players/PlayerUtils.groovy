package app.players;

import groovy.transform.CompileStatic;

@CompileStatic
class PlayerUtils {

    private static final int SEEK_BAR_INTERVAL = 1000;

    static int calculateProgressForSeekBar(int progress) {
        (progress / (float) SEEK_BAR_INTERVAL) as int
    }

    static int convertSeekBarToProgress(int seekBarProgress) {
        seekBarProgress * SEEK_BAR_INTERVAL
    }

}
