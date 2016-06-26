package app.ui.activity

import android.os.Bundle
import app.App
import app.R
import com.github.s0nerik.betterknife.annotations.InjectLayout
import groovy.transform.CompileStatic

@CompileStatic
@InjectLayout(value = R.layout.activity_local_playback, injectAllViews = true)
class LocalPlaybackActivity extends PlaybackActivity {

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        App.get().inject(this)
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }

//    @Subscribe
//    @Override
//    protected void onClientConnected(ClientConnectedEvent event) {
//        super.onClientConnected(event);
//    }

//    @Subscribe
//    @Override
//    protected void onClientDisconnected(ClientDisconnectedEvent event) {
//        super.onClientDisconnected(event);
//    }
}