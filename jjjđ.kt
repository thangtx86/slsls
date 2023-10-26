package io.github.thangtx86.myapplication

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class ColorPickerRingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var initialAngle = 270F
    private val colors = listOf(
        Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.LTGRAY
    )
    private var selectedColor = Color.RED

    private var centerX = 0F
    private var centerY = 0F
    private var outerRadius = 0F
    private var innerRadius = 0F
    private var thumbRadius = 20F
    private var touchAngle = 0F
    private var listener: OnColorChangedListener? = null

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var isDragging = false
    private val backgroundImage: Drawable = context.resources.getDrawable(R.drawable.test, null)

    init {
        selectedColor = colors[0]
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    interface OnColorChangedListener {
        fun onColorChanged(color: Int)
    }

    fun setOnColorChangedListener(listener: OnColorChangedListener) {
        this.listener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = (w / 2).toFloat()
        centerY = (h / 2).toFloat()
        outerRadius = (min(w, h) / 2 - context.dpToPx(80)).toFloat()
//        outerRadius = (min(w, h) / 2).toFloat()
        innerRadius = outerRadius -140F
    }

    override fun onDraw(canvas: Canvas) {
        val totalColors = colors.size
        val sweepAngle = 360F / totalColors

        val paint = Paint()
        paint.isAntiAlias = true

        for (index in 0 until totalColors) {
            val startAngle = (index * sweepAngle + initialAngle) % 360

//            // Vẽ hình màu xanh bên trong innerRadius
//            val greenCirclePaint = Paint()
//            greenCirclePaint.color = Color.GREEN
//            greenCirclePaint.style = Paint.Style.FILL
//            canvas.drawCircle(centerX, centerY, innerRadius, greenCirclePaint)

            // Vẽ màu
            paint.color = colors[index]
            paint.style = Paint.Style.FILL
            val rectF = RectF(
                centerX - outerRadius,
                centerY - outerRadius,
                centerX + outerRadius,
                centerY + outerRadius
            )

            val path = Path()
            path.arcTo(rectF, startAngle, sweepAngle)
            path.lineTo(centerX, centerY)
            path.close()
            canvas.drawPath(path, paint)
        }

        // Vẽ vòng tròn ở trung tâm
        canvas.drawCircle(centerX, centerY, innerRadius, clearPaint)

        // Vẽ backgroundImage
        val imageWidth = ((outerRadius + context.dpToPx(45)) * 2)
        val imageHeight = ((outerRadius + context.dpToPx(45)) * 2)

        val imageX = centerX - (outerRadius + context.dpToPx(45))
        val imageY = centerY - (outerRadius + context.dpToPx(45))

        backgroundImage.setBounds(
            imageX.toInt(),
            imageY.toInt(),
            (imageX + imageWidth).toInt(),
            (imageY + imageHeight).toInt()
        )
        backgroundImage.draw(canvas)

        // Vẽ nút chọn màu
        val distanceFromCenter = (outerRadius + innerRadius) / 2
        val x = centerX + distanceFromCenter * cos(Math.toRadians(touchAngle.toDouble())).toFloat()
        val y = centerY + distanceFromCenter * sin(Math.toRadians(touchAngle.toDouble())).toFloat()

        val thumbPaint = Paint()
        thumbPaint.color = Color.BLACK
        thumbPaint.style = Paint.Style.STROKE
        thumbPaint.strokeWidth = 2F
        canvas.drawCircle(x, y, thumbRadius, thumbPaint)

        val selectedThumbPaint = Paint()
        selectedThumbPaint.color = selectedColor
        selectedThumbPaint.style = Paint.Style.FILL
        canvas.drawCircle(
            x, y, thumbRadius - 4, selectedThumbPaint
        )

        val blackCirclePaint = Paint()
        blackCirclePaint.color = Color.BLACK
        blackCirclePaint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, innerRadius, blackCirclePaint)
        val buttonRadiusDp = 16
        val buttonRadius = context.dpToPx(buttonRadiusDp)
        // Vẽ button bên trong blackCirclePaint
        val buttonPaint = Paint()
        buttonPaint.color = Color.BLUE // Màu của button (có thể thay đổi)
        buttonPaint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, buttonRadius/1f, buttonPaint)

// Vẽ chữ "OK" bên trong button
        val textPaint = Paint()
        textPaint.color = Color.WHITE // Màu của chữ (có thể thay đổi)
        textPaint.textSize = context.dpToPx(12) /1f// Kích thước của chữ (điều chỉnh theo ý muốn)
        textPaint.textAlign = Paint.Align.CENTER
        val textX = centerX
        val textY = centerY + textPaint.textSize / 2
        canvas.drawText("OK", textX, textY, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        val centerX = width / 2
        val centerY = height / 2
        val distanceFromCenter = (innerRadius + outerRadius) / 2

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = x - centerX
                val touchY = y - centerY
                // Kiểm tra xem điểm chạm có nằm trong vùng của nút chọn màu không
                if (sqrt(touchX * touchX + touchY * touchY) <= distanceFromCenter + thumbRadius) {
                    isDragging = true
                    touchAngle = getAngle(x, y)
                    updateSelectedColor()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    touchAngle = getAngle(x, y)
                    updateSelectedColor()
                }
            }

            MotionEvent.ACTION_UP -> {
                isDragging = false
            }
        }

        invalidate()

        return true
    }


    private fun isTouchInsideThumb(x: Float, y: Float): Boolean {
        val thumbX = centerX + innerRadius * cos(Math.toRadians(touchAngle.toDouble())).toFloat()
        val thumbY = centerY + innerRadius * sin(Math.toRadians(touchAngle.toDouble())).toFloat()
        val distance = sqrt((x - thumbX).pow(2) + (y - thumbY).pow(2))
        return distance <= thumbRadius
    }

    private fun getAngle(x: Float, y: Float): Float {
        return Math.toDegrees(atan2(y - centerY, x - centerX).toDouble()).toFloat().let { angle ->
            if (angle < 0) angle + 360 else angle
        }
    }

    private fun updateSelectedColor() {
        val colorIndex = ((touchAngle / 360) * colors.size).toInt()
        selectedColor = colors[colorIndex]
        listener?.onColorChanged(selectedColor)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size =
            min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(size, size)
    }

    fun Context.dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}


