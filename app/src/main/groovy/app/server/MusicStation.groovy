package app.server
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import app.Daggered
import app.player.LocalPlayer
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import ru.noties.debug.Debug
import rx.Observable

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

        def listener = { String methodName ->
            [
                    onSuccess: { Debug.d "${methodName} onSuccess" },
                    onFailure: { int reason ->
                        Debug.d "${methodName} onFailure (${reason})"
                    }
            ] as WifiP2pManager.ActionListener
        }

        manager.discoverPeers channel, listener("discoverPeers")
        manager.addLocalService channel, serviceInfo, listener("addLocalService")
        manager.requestConnectionInfo channel, { WifiP2pInfo info ->
            if (info.groupFormed) {
                if (info.isGroupOwner) {
                    def l = [
                            onSuccess: {
                                Debug.d "requestConnectionInfo onSuccess"
                                manager.createGroup channel, listener("createGroup")
                            },
                            onFailure: { int reason ->
                                Debug.d "requestConnectionInfo onFailure (${reason})"
                            }
                    ] as WifiP2pManager.ActionListener
                    manager.removeGroup channel, l
                } else {
                    Debug.e "info.groupFormed == true, info.isGroupOwner == false"
                }
            } else {
                Debug.d "info.groupFormed == false"
                manager.createGroup channel, listener("createGroup")
            }
        } as WifiP2pManager.ConnectionInfoListener
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