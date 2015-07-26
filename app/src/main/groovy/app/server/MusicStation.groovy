package app.server
import android.content.Context
import android.net.wifi.p2p.WifiP2pInfo
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

import static java.lang.reflect.Modifier.*

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

    public static final String INSTANCE_NAME = "_LWM";
    public static final String SERVICE_TYPE = "_http._tcp";

    boolean enabled = false

    private WifiP2pManager.Channel channel
    private WifiP2pDnsSdServiceInfo serviceInfo

    private MusicServer server = new MusicServer()

    private String getCause(int code) {
        manager.class.declaredFields.find {
            it.modifiers == (PUBLIC | STATIC | FINAL) && it.get(manager) == code
        }.name
    }

    WifiP2pManager.ActionListener debugP2PActionListener(String methodName) {
        [
                onSuccess: { Debug.d "${methodName} success." },
                onFailure: { int code ->
                    Debug.e "${methodName} failure. Error: ${getCause(code)}."
                    disable()
                }
        ] as WifiP2pManager.ActionListener
    }

    WifiP2pManager.ActionListener groupCreationListener = [
            onSuccess: {
                Debug.d "createGroup success."
                enabled = true
                bus.post new StateChangedEvent(StateChangedEvent.State.ENABLED)
            },
            onFailure: { int code ->
                Debug.e "createGroup failure. Error: ${getCause(code)}."
                disable()
            }
    ] as WifiP2pManager.ActionListener

    WifiP2pManager.ActionListener groupRemovingListener = [
            onSuccess: {
                Debug.d "removeGroup success."
                enabled = false
                bus.post new StateChangedEvent(StateChangedEvent.State.DISABLED)
            },
            onFailure: { int code ->
                Debug.e "removeGroup failure. Error: ${getCause(code)}."
            }
    ] as WifiP2pManager.ActionListener

    private void startServiceRegistrationAndCreateGroup() {
        def record = [port: StreamServer.PORT,
                      name : "station",
                      currentSong : "Asking Alexandria - Closure"]

        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance INSTANCE_NAME, SERVICE_TYPE, record

        channel = manager.initialize context, context.mainLooper, { Debug.d "Channel disconnected" }

        manager.addLocalService channel, serviceInfo, debugP2PActionListener("addLocalService")
        manager.requestConnectionInfo channel, { WifiP2pInfo info ->
            if (info.groupFormed) {
                if (info.isGroupOwner) {
                    manager.removeGroup channel, [
                            onSuccess: {
                                Debug.d "removeGroup success."
                                manager.createGroup channel, groupCreationListener
                            },
                            onFailure: { int code ->
                                Debug.e "removeGroup failure. Error: ${getCause(code)}."
                                disable()
                            }
                    ] as WifiP2pManager.ActionListener
                } else {
                    Debug.w "info.groupFormed == true, info.isGroupOwner == false"
                    manager.createGroup channel, groupCreationListener
                }
            } else {
                Debug.d "info.groupFormed == false"
                manager.createGroup channel, groupCreationListener
            }
        } as WifiP2pManager.ConnectionInfoListener
        manager.discoverPeers channel, debugP2PActionListener("discoverPeers")
    }

    private void removeServiceRegistrationAndGroup() {
        manager.removeLocalService channel, serviceInfo, debugP2PActionListener("removeLocalService")
        manager.removeGroup channel, groupRemovingListener
    }

    void toggleEnabledState() {
        if (enabled) disable()
        else enable()
    }

    void enable() {
        bus.post new StateChangedEvent(StateChangedEvent.State.CHANGING)
        server.start()
        startServiceRegistrationAndCreateGroup()
    }

    void disable() {
        bus.post new StateChangedEvent(StateChangedEvent.State.CHANGING)
        server.stop()
        removeServiceRegistrationAndGroup()
    }

    @TupleConstructor
    static class StateChangedEvent {
        enum State { CHANGING, DISABLED, ENABLED }
        State state
    }

}