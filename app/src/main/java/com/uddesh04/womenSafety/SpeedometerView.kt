package com.uddesh04.womenSafety

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class SpeedometerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var maxSpeed: Double = DEFAULT_MAX_SPEED
    private var speed: Double = 0.0
    private val ranges = mutableListOf<ColoredRange>()

    private var majorTickStep: Double = DEFAULT_MAJOR_TICK_STEP
    private var minorTicks: Int = DEFAULT_MINOR_TICKS

    private var backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var backgroundInnerPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var needlePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var ticksPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var colorLinePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        attrs?.let {
            val a = context.theme.obtainStyledAttributes(it, R.styleable.SpeedometerView, 0, 0)
            try {
                maxSpeed = a.getFloat(R.styleable.SpeedometerView_SpeedometerView_maxSpeed, DEFAULT_MAX_SPEED.toFloat()).toDouble()
                speed = a.getFloat(R.styleable.SpeedometerView_SpeedometerView_speed, 0f).toDouble()
            } finally {
                a.recycle()
            }
        }
        initPaints()
    }

    private fun initPaints() {
        backgroundPaint.color = Color.LTGRAY
        backgroundPaint.style = Paint.Style.FILL

        backgroundInnerPaint.color = Color.DKGRAY
        backgroundInnerPaint.style = Paint.Style.FILL

        needlePaint.color = Color.RED
        needlePaint.style = Paint.Style.STROKE
        needlePaint.strokeWidth = 5f

        ticksPaint.color = Color.BLACK
        ticksPaint.style = Paint.Style.STROKE
        ticksPaint.strokeWidth = 3f

        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 18f

        colorLinePaint.color = Color.GREEN
        colorLinePaint.style = Paint.Style.STROKE
        colorLinePaint.strokeWidth = 5f
    }

    fun setValue(value: Float) {
        speed = value.toDouble()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT)

        drawBackground(canvas)
        drawTicks(canvas)
        drawNeedle(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        val oval = getOval(canvas, 1f)
        canvas.drawArc(oval, 180f, 180f, true, backgroundPaint)

        val innerOval = getOval(canvas, 0.9f)
        canvas.drawArc(innerOval, 180f, 180f, true, backgroundInnerPaint)
    }

    private fun drawTicks(canvas: Canvas) {
        val oval = getOval(canvas, 1f)
        val radius = oval.width() * 0.35f
        val majorStep = (majorTickStep / maxSpeed * 160).toFloat()
        val minorStep = majorStep / (1 + minorTicks)

        var currentAngle = 10f
        while (currentAngle <= 170f) {
            drawTick(canvas, oval, radius, currentAngle, true)

            for (i in 1..minorTicks) {
                val angle = currentAngle + i * minorStep
                if (angle >= 170 + minorStep / 2) break
                drawTick(canvas, oval, radius, angle, false)
            }
            currentAngle += majorStep
        }
    }

    private fun drawTick(canvas: Canvas, oval: RectF, radius: Float, angle: Float, major: Boolean) {
        val tickLength = if (major) 30f else 15f
        canvas.drawLine(
            (oval.centerX() + cos(Math.toRadians((180 - angle).toDouble())) * (radius - tickLength / 2)).toFloat(),
            (oval.centerY() - sin(Math.toRadians(angle.toDouble())) * (radius - tickLength / 2)).toFloat(),
            (oval.centerX() + cos(Math.toRadians((180 - angle).toDouble())) * (radius + tickLength / 2)).toFloat(),
            (oval.centerY() - sin(Math.toRadians(angle.toDouble())) * (radius + tickLength / 2)).toFloat(),
            ticksPaint
        )
    }

    private fun drawNeedle(canvas: Canvas) {
        val oval = getOval(canvas, 1f)
        val radius = oval.width() * 0.35f
        val angle = 10 + (speed / maxSpeed * 160).toFloat()

        canvas.drawLine(
            oval.centerX(),
            oval.centerY(),
            (oval.centerX() + cos(Math.toRadians((180 - angle).toDouble())) * radius).toFloat(),
            (oval.centerY() - sin(Math.toRadians(angle.toDouble())) * radius).toFloat(),
            needlePaint
        )

        val smallOval = getOval(canvas, 0.2f)
        canvas.drawArc(smallOval, 180f, 180f, true, backgroundPaint)
    }

    private fun getOval(canvas: Canvas, factor: Float): RectF {
        val width = canvas.width - paddingLeft - paddingRight
        val height = canvas.height - paddingTop - paddingBottom

        val size = min(width, height * 2) * factor
        return RectF(0f, 0f, size, size).apply {
            offset(
                ((width - this.width()) / 2 + paddingLeft).toFloat(),
                ((height * 2 - this.height()) / 2 + paddingTop).toFloat()
            )
        }
    }

    fun setMaxSpeed(maxSpeed: Double) {
        if (maxSpeed <= 0) throw IllegalArgumentException("Max speed must be positive.")
        this.maxSpeed = maxSpeed
        invalidate()
    }

    fun setSpeed(speed: Double) {
        if (speed < 0) throw IllegalArgumentException("Speed must be positive.")
        this.speed = speed.coerceAtMost(maxSpeed)
        invalidate()
    }

    fun animateSpeed(speed: Double, duration: Long = 1500, startDelay: Long = 200) {
        if (speed < 0) throw IllegalArgumentException("Speed must be positive.")
        val animator = ValueAnimator.ofFloat(this.speed.toFloat(), speed.toFloat())
        animator.duration = duration
        animator.startDelay = startDelay
        animator.addUpdateListener { animation ->
            this.speed = (animation.animatedValue as Float).toDouble()
            invalidate()
        }
        animator.start()
    }

    companion object {
        const val DEFAULT_MAX_SPEED = 100.0
        const val DEFAULT_MAJOR_TICK_STEP = 20.0
        const val DEFAULT_MINOR_TICKS = 1
    }

    data class ColoredRange(val color: Int, val begin: Double, val end: Double)
}