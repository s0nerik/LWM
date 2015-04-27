package app.player;

public class PlayerUtils {

    private static final int SEEK_BAR_INTERVAL = 1000;

    public static int calculateProgressForSeekBar(int progress) {
        return (int) (progress / (float) SEEK_BAR_INTERVAL);
    }

    public static int convertSeekBarToProgress(int seekBarProgress) {
        return seekBarProgress * SEEK_BAR_INTERVAL;
    }

}
