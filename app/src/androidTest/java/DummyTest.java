import android.app.Activity;

import com.lwm.app.ui.activity.LocalSongChooserActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DummyTest {

    @Test
    public void startLocalSongChooser() throws Exception {
        Activity activity = Robolectric.buildActivity(LocalSongChooserActivity.class).get();
    }

}