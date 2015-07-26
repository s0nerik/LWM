package app.ui.custom_view
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import app.Injector
import app.R
import app.Utils
import app.server.MusicStation
import app.server.MusicStation.StateChangedEvent
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import ru.noties.debug.Debug

import javax.inject.Inject

import static app.server.MusicStation.StateChangedEvent.State.*

@PackageScope(PackageScopeTarget.FIELDS)
@CompileStatic
class BroadcastButton extends RelativeLayout {

    @Inject
    Bus bus

    @Inject
    MusicStation musicStation

    @Inject
    Utils utils

    @Inject
    Resources resources

    @InjectView(R.id.icon)
    ImageView icon
    @InjectView(R.id.progress)
    ProgressBar progress

    BroadcastButton(Context context) {
        super(context)
        init()
    }

    BroadcastButton(Context context, AttributeSet attrs) {
        super(context, attrs)
        init()
    }

    BroadcastButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr)
        init()
    }

    private void init() {
        inflate context, R.layout.layout_btn_broadcast, this
        BetterKnife.inject this, this
        Injector.inject this
        broadcastState = musicStation.enabled
        onClickListener = {
            musicStation.toggleEnabledState()
        }
    }

    private void setProgressVisibility(boolean show) {
        progress.visibility = show ? VISIBLE : GONE
        icon.visibility = show ? GONE : VISIBLE
    }

    private void setBroadcastState(boolean isBroadcasting) {
        icon.imageResource = isBroadcasting ? R.drawable.ic_ap_on : R.drawable.ic_ap_off
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow()
        Debug.d "BroadcastButton onAttachedToWindow"
        bus.register this
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Debug.d "BroadcastButton onDetachedFromWindow"
        bus.unregister this
    }

    @Subscribe
    void onMusicStationStateChangedEvent(StateChangedEvent event) {
        switch (event.state) {
            case CHANGING:
                progressVisibility = true
                break
            case DISABLED:
                progressVisibility = false
                broadcastState = false
                break
            case ENABLED:
                progressVisibility = false
                broadcastState = true
                break
        }
    }
}
