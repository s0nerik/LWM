package app.ui.activity

import app.Utils
import app.ui.base.BaseActivity
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
public abstract class PlaybackActivity extends BaseActivity {

    @Inject
    protected Utils utils

//    protected void onClientConnected(ClientConnectedEvent event) {
//        Croutons.clientConnected(this, event.getClientInfo()).show();
//    }
//
//    protected void onClientDisconnected(ClientDisconnectedEvent event) {
//        Croutons.clientDisconnected(this, event.getClientInfo()).show();
//    }

}