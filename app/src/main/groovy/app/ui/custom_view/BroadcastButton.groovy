package app.ui.custom_view

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import app.Injector
import app.R
import app.events.access_point.AccessPointStateEvent
import app.helper.wifi.WifiAP
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.InjectView
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import ru.noties.debug.Debug

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class BroadcastButton extends RelativeLayout {

    @Inject
    Bus bus;

    @Inject
    WifiAP wifiAP;

    @Inject
    Resources resources;

    @InjectView(R.id.icon)
    ImageView mIcon;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

    public BroadcastButton(Context context) {
        super(context);
        init();
    }

    public BroadcastButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BroadcastButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_btn_broadcast, this);
        SwissKnife.inject(this, this);
        Injector.inject(this);
        setBroadcastState(wifiAP.isEnabled());
        onClickListener = { wifiAP.toggleWiFiAP() }
    }

    private void setProgressVisibility(boolean show) {
        mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        mIcon.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void setBroadcastState(boolean isBroadcasting) {
        mIcon.setImageResource(isBroadcasting ? R.drawable.ic_ap_on : R.drawable.ic_ap_off);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Debug.d("BroadcastButton onAttachedToWindow");
        bus.register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Debug.d("BroadcastButton onDetachedFromWindow");
        bus.unregister(this);
    }

    @Subscribe
    public void onAccessPointStateEvent(AccessPointStateEvent event) {
        switch (event.getState()) {
            case AccessPointStateEvent.State.CHANGING:
                setProgressVisibility(true);
                break;
            case AccessPointStateEvent.State.DISABLED:
                setProgressVisibility(false);
                setBroadcastState(false);
                break;
            case AccessPointStateEvent.State.ENABLED:
                setProgressVisibility(false);
                setBroadcastState(true);
                break;
        }
    }
}
