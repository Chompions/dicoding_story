package com.sawelo.dicoding_story.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.InputType
import android.util.AttributeSet
import android.util.Patterns
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.sawelo.dicoding_story.R

class CustomEditText : AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val idleBackground = ContextCompat.getDrawable(context, R.drawable.rounded_bg)
    private val errorBackground = ContextCompat.getDrawable(context, R.drawable.rounded_bg_error)

    private val typedValue: TypedValue = TypedValue()
    private val paint = Paint()

    private var focusedOnce = false

    init {
        background = idleBackground
        context.theme.resolveAttribute(R.attr.customInverseColor, typedValue, true)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.textSize = 30F
        paint.color = typedValue.data

        when {
            (inputType and InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) -> {
                if (!Patterns.EMAIL_ADDRESS.matcher(text.toString()).matches() && focusedOnce) {
                    background = errorBackground
                    val text = context.getString(R.string.error_email)
                    canvas?.drawText(text, 10F, (height + 50).toFloat(), paint)
                } else {
                    background = idleBackground
                }
            }
            (inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD == InputType.TYPE_TEXT_VARIATION_PASSWORD) -> {
                if (text?.length!! < 6 && focusedOnce) {
                    background = errorBackground
                    val text = context.getString(R.string.error_password)
                    canvas?.drawText(text, 10F, (height + 50).toFloat(), paint)
                } else {
                    background = idleBackground
                }
            }
        }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) focusedOnce = true
    }
}