package app.ui.custom_view

import android.animation.FloatArrayEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import app.R
import groovy.transform.CompileStatic

@CompileStatic
class RadialEqualizerView extends View {

    private Paint equalizerPaint
    private RectF bounds
    private RectF dirtyBounds

    int minScale
    int barsCount

    float[] percents

    RadialEqualizerView(Context context, AttributeSet attrs) {
        super(context, attrs)
        init attrs
    }

    RadialEqualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr)
        init attrs
    }

    RadialEqualizerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes)
        init attrs
    }

    private void init(AttributeSet attrs) {
        bounds = new RectF(0f, 0f, 100f, 100f)
        dirtyBounds = new RectF(bounds)

        def a = context.obtainStyledAttributes attrs, R.styleable.RadialEqualizerView as int[]

        minScale = a.getInt R.styleable.RadialEqualizerView_rew_min_scale, 75

        equalizerPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
        equalizerPaint.style = Paint.Style.FILL
        equalizerPaint.color = a.getColor R.styleable.RadialEqualizerView_rew_color, Color.BLACK

        barsCount = a.getInt R.styleable.RadialEqualizerView_rew_bars_count, 10

        percents = new float[barsCount]

        if (inEditMode) {
            randomize()
        }

        a.recycle()
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh)

        bounds.set 0, 0, w, w
        dirtyBounds.set bounds
    }

    void randomize() {
        def newPercents = new float[barsCount]
        def rand = new Random()

        def baseLevel = rand.nextFloat() * 0.5f

        for (int i = 0; i < barsCount; i++) {
            newPercents[i] = 100f * (baseLevel + rand.nextFloat() * 0.5f) as float
            if (newPercents[i] > 100f)
                newPercents[i] = 100f
        }

        if (inEditMode) {
            percents = newPercents
            invalidate()
            return
        }

        def oldPercents = Arrays.copyOf(percents, percents.length)

        def animator = ObjectAnimator.ofObject(this, "percents", new FloatArrayEvaluator(), oldPercents, newPercents)
        animator.duration = 200

        animator.start()
    }

    void setPercents(float[] percents) {
        this.percents = percents
        invalidate()
    }

    private void scaleBounds(RectF bounds, float scale) {
        bounds.left = (bounds.right / 2f) - ((bounds.right / 2f) * scale) as float
        bounds.top = (bounds.bottom / 2f) - ((bounds.bottom / 2f) * scale) as float

        bounds.right = (bounds.right / 2f) + ((bounds.right / 2f) * scale) as float
        bounds.bottom = (bounds.bottom / 2f) + ((bounds.bottom / 2f) * scale) as float
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float startAngle = 360f / percents.size() as float

        // Draw the pie slices
        for (int i = 0; i < percents.size(); i++) {
            float scale = percents[i] / 100f as float
            scale = (minScale + ((100 - minScale) * scale)) / 100f as float
            scaleBounds(dirtyBounds, scale)

            canvas.drawArc dirtyBounds, startAngle * i as float, startAngle as float, true, equalizerPaint

            dirtyBounds.set bounds
        }
    }

}