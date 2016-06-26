package app.server

import android.content.Context
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import app.App
import app.Config
import com.github.s0nerik.rxbus.RxBus
import app.websocket.WebSocketMessageServer
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import ru.noties.debug.Debug

import javax.inject.Inject

import static java.lang.reflect.Modifier.FINAL
import static java.lang.reflect.Modifier.PUBLIC
import static java.lang.reflect.Modifier.STATIC

@CompileStatic
class MusicStation {

    enum State { CHANGING, DISABLED, ENABLED }

    @Inject
    protected WifiP2pManager manager

    @Inject
    protected Context context

    @Inject
    protected WebSocketMessageServer server

    public static final String INSTANCE_NAME = "_LWM";
    public static final String SERVICE_TYPE = "_http._tcp";

    private WifiP2pManager.Channel channel
    private WifiP2pDnsSdServiceInfo serviceInfo

    State state = State.DISABLED

    MusicStation() {
        App.get().inject(this)
    }

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
                setState State.ENABLED
            },
            onFailure: { int code ->
                Debug.e "createGroup failure. Error: ${getCause(code)}."
                setState State.DISABLED
            }
    ] as WifiP2pManager.ActionListener

    WifiP2pManager.ActionListener groupRemovingListener = [
            onSuccess: {
                Debug.d "removeGroup success."
                setState State.DISABLED
            },
            onFailure: { int code ->
                Debug.e "removeGroup failure. Error: ${getCause(code)}."
                setState State.ENABLED
            }
    ] as WifiP2pManager.ActionListener

    private void startServiceRegistrationAndCreateGroup() {
        def record = [port: Config.HTTP_SERVER_PORT as String,
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

    private void setState(State newState) {
        state = newState
        RxBus.post new BroadcastStateChangedEvent(newState)
    }

    private void removeServiceRegistrationAndGroup() {
        manager.removeLocalService channel, serviceInfo, debugP2PActionListener("removeLocalService")
        manager.removeGroup channel, groupRemovingListener
    }

    void toggleEnabledState() {
        switch (state) {
            case State.CHANGING:
                break
            case State.DISABLED:
                enable()
                break
            case State.ENABLED:
                disable()
                break
        }
    }

    void enable() {
        if (server.started) return
        setState State.CHANGING
        server.startAsObservable()
              .doOnCompleted { startServiceRegistrationAndCreateGroup() }
              .subscribe()
    }

    void disable() {
        if (!server.started) return
        setState State.CHANGING
        server.stopAsObservable()
              .doOnCompleted { removeServiceRegistrationAndGroup() }
              .subscribe()
    }

    @TupleConstructor
    static class BroadcastStateChangedEvent {
        State state
    }

}