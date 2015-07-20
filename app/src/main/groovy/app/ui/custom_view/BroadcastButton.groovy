package app.ui.custom_view
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import app.Injector
import app.R
import app.server.MusicStation
import app.server.MusicStation.StateChangedEvent
import app.service.MusicStationService
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug

import javax.inject.Inject

import static app.server.MusicStation.StateChangedEvent.State.*

@CompileStatic
class BroadcastButton extends RelativeLayout {

    @Inject
    @PackageScope
    Bus bus

    @Inject
    @PackageScope
    MusicStation musicStation

    @Inject
    @PackageScope
    Resources resources

    @InjectView(R.id.icon)
    ImageView mIcon
    @InjectView(R.id.progress)
    ProgressBar mProgress

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
            context.startService new Intent(context, MusicStationService)
//            musicStation.toggleEnabledState()
        }
    }

    private void setProgressVisibility(boolean show) {
        mProgress.visibility = show ? VISIBLE : GONE
        mIcon.visibility = show ? GONE : VISIBLE
    }

    private void setBroadcastState(boolean isBroadcasting) {
        mIcon.imageResource = isBroadcasting ? R.drawable.ic_ap_on : R.drawable.ic_ap_off
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
