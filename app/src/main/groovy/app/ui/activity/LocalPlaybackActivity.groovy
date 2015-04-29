package app.ui.activity;

import android.os.Bundle;

import app.R;
import app.events.server.ClientConnectedEvent;
import app.events.server.ClientDisconnectedEvent;
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget;

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class LocalPlaybackActivity extends PlaybackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_playback);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }

    @Subscribe
    @Override
    protected void onClientConnected(ClientConnectedEvent event) {
        super.onClientConnected(event);
    }

    @Subscribe
    @Override
    protected void onClientDisconnected(ClientDisconnectedEvent event) {
        super.onClientDisconnected(event);
    }
}