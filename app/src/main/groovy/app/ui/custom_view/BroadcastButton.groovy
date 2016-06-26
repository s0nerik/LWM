package app.ui.custom_view

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import app.App
import app.R
import app.Utils
import com.github.s0nerik.rxbus.RxBus
import app.server.MusicStation
import app.server.MusicStation.BroadcastStateChangedEvent
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import groovy.transform.CompileStatic
import rx.Subscription

import javax.inject.Inject

import static app.server.MusicStation.State.CHANGING
import static app.server.MusicStation.State.ENABLED

@CompileStatic
class BroadcastButton extends RelativeLayout {

    private Subscription subscription

    @Inject
    protected MusicStation musicStation
    @Inject
    protected Utils utils
    @Inject
    protected Resources resources

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
        if (!inEditMode) {
            App.get().inject this
            broadcastState = musicStation.state
            onClickListener = {
                musicStation.toggleEnabledState()
            }
        }
    }

    private void setProgressVisibility(boolean show) {
        progress.visibility = show ? VISIBLE : GONE
        icon.visibility = show ? GONE : VISIBLE
    }

    private void setBroadcastState(MusicStation.State state) {
        if (state == CHANGING) {
            setProgressVisibility true
        } else {
            setProgressVisibility false
            icon.imageResource = state == ENABLED ? R.drawable.ic_ap_on : R.drawable.ic_ap_off
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!inEditMode)
            subscription = RxBus.on(BroadcastStateChangedEvent).subscribe(this.&onEvent)
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (!inEditMode)
            subscription?.unsubscribe()
    }

    private void onEvent(BroadcastStateChangedEvent event) {
        broadcastState = event.state
    }
}
