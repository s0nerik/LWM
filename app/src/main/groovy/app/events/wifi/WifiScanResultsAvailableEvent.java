package app.events.wifi;

import android.net.wifi.ScanResult;

import java.util.List;

public class WifiScanResultsAvailableEvent {

    private List<ScanResult> scanResults;

    public WifiScanResultsAvailableEvent(List<ScanResult> scanResults) {
        this.scanResults = scanResults;
    }

    public List<ScanResult> getScanResults() {
        return scanResults;
    }
}
