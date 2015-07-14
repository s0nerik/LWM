package app.events.p2p
import android.content.Intent
import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
public class P2PBroadcastReceivedEvent {

    final Intent intent

}
