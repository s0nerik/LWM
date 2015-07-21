package app.server
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import app.Daggered
import app.player.LocalPlayer
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import ru.noties.debug.Debug

import javax.inject.Inject

@CompileStatic
class MusicStation extends Daggered {

    @Inject
    @PackageScope
    WifiP2pManager manager

    @Inject
    @PackageScope
    Context context

    @Inject
    @PackageScope
    LocalPlayer player

    @Inject
    @PackageScope
    Bus bus

    boolean enabled = false

    private WifiP2pManager.Channel channel
    private WifiP2pDnsSdServiceInfo serviceInfo

    private void startServiceRegistration() {
        def record = [listen_port: StreamServer.PORT,
                      station_name : "station",
                      current_song : "Asking Alexandria - Closure"]

        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance "_test", "_presence._tcp", record

        channel = manager.initialize context, context.mainLooper, { Debug.d "Channel disconnected" }

        manager.discoverPeers channel,
                [onSuccess: { Debug.d "discoverPeers onSuccess" },
                 onFailure: { int reason ->
                     Debug.d "discoverPeers onFailure (${reason})"
                 }] as WifiP2pManager.ActionListener

        manager.addLocalService channel, serviceInfo, [
                onSuccess: { Debug.d "addLocalService onSuccess" },
                onFailure: { int arg0 -> Debug.d "addLocalService onFailure: ${arg0}" }
        ] as WifiP2pManager.ActionListener

        manager.createGroup channel, [
                onSuccess: { Debug.d "createGroup onSuccess" },
                onFailure: { int arg0 -> Debug.d "createGroup onFailure: ${arg0}" }
        ] as WifiP2pManager.ActionListener
    }

    private void removeServiceRegistration() {
        manager.removeLocalService channel, serviceInfo, [
                onSuccess: { Debug.d "removeLocalService onSuccess" },
                onFailure: { int arg0 -> Debug.d "removeLocalService onFailure: ${arg0}" }
        ] as WifiP2pManager.ActionListener
    }

    void toggleEnabledState() {
        if (enabled) disable()
        else enable()
    }

    void enable() {
        new MusicServer().start()
        startServiceRegistration()
        enabled = true
        bus.post(new StateChangedEvent(StateChangedEvent.State.ENABLED))
    }

    void disable() {
        removeServiceRegistration()
        enabled = false
        bus.post(new StateChangedEvent(StateChangedEvent.State.DISABLED))
    }

    @TupleConstructor
    static class StateChangedEvent {
        enum State { CHANGING, DISABLED, ENABLED }
        State state
    }

}