package app.adapter

import android.content.Context
import android.content.res.Resources
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import app.helper.wifi.WifiAP
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.InjectView
import groovy.transform.CompileStatic

@CompileStatic
public class StationsAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context context;
    private List<ScanResult> stations;

    private int signalLevels;
    private String[] signalLevelNames;

    public StationsAdapter(Context context, List<ScanResult> stations) {
        this.stations = new ArrayList<>();

        for (ScanResult station : stations) {
            if (station.SSID.matches(WifiAP.AP_NAME_REGEXP)) {
                this.stations.add(station);
            }
        }

        Resources res = context.getResources();
        signalLevels = res.getInteger(R.integer.wifi_max_level);
        signalLevelNames = res.getStringArray(R.array.wifi_signal_levels);

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return stations.size();
    }

    @Override
    public ScanResult getItem(int position) {
        return stations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_players_around, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ScanResult result = getItem(position);

        def m = result.SSID =~ WifiAP.AP_NAME_REGEXP
        holder.mName.text = m[0][1]

        int signalLevel = WifiManager.calculateSignalLevel(getItem(position).level, signalLevels);
        String signal = signalLevelNames[signalLevel];

        holder.mSignal.setText("Signal: "+signal);

        return convertView;
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'list_item_players_around.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Inmite Developers (http://inmite.github.io)
     */
    static class ViewHolder {
        @InjectView(R.id.name)
        TextView mName;
        @InjectView(R.id.signal)
        TextView mSignal;

        ViewHolder(View view) {
            SwissKnife.inject(this, view);
        }
    }

}