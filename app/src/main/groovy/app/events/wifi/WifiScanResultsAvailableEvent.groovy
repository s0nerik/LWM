package app.events.wifi
import android.net.wifi.ScanResult
import groovy.transform.CompileStatic

@CompileStatic
public class WifiScanResultsAvailableEvent {

    List<ScanResult> scanResults;

    public WifiScanResultsAvailableEvent(List<ScanResult> scanResults) {
        this.scanResults = scanResults;
    }
}
