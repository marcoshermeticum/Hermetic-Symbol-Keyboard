package com.hermetic.keyboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hermetic.keyboard.R

/**
 * Number and special characters keyboard view.
 * Two pages: numbers+common symbols, and more symbols.
 */
class NumberSymbolsView(
    context: Context,
    private val onKeyPress: (String) -> Unit
) : LinearLayout(context) {

    private var page = 0 // 0 = numbers, 1 = more symbols

    init {
        orientation = VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.background))
        setPadding(dpToPx(3), dpToPx(6), dpToPx(3), dpToPx(6))
        buildPage()
    }

    private fun buildPage() {
        removeAllViews()
        val rows = if (page == 0) PAGE_1 else PAGE_2
        rows.forEach { row -> addView(createRow(row)) }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createRow(keys: List<String>): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(54)).apply {
                topMargin = dpToPx(4)
            }
            keys.forEach { key ->
                val isSpecial = key in listOf("ABC", "⌫", "↵", "MAIS", " ")
                val weight = when {
                    key == " " -> 3f
                    isSpecial -> 1.5f
                    else -> 1f
                }
                addView(TextView(context).apply {
                    text = when (key) {
                        " " -> ""
                        "MAIS" -> if (page == 0) "#+=" else "123"
                        else -> key
                    }
                    gravity = Gravity.CENTER
                    setTextColor(ContextCompat.getColor(context, R.color.key_text))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isSpecial) 16f else 20f)
                    setBackgroundResource(R.drawable.key_background)
                    layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, weight).apply {
                        marginStart = dpToPx(3); marginEnd = dpToPx(3)
                    }
                    isFocusable = false
                    isClickable = true

                    setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                v.isPressed = true
                                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                handleKey(key)
                                true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                v.isPressed = false; true
                            }
                            else -> false
                        }
                    }
                })
            }
        }
    }

    private fun handleKey(key: String) {
        when (key) {
            "ABC" -> onKeyPress("SWITCH_TO_ALPHA")
            "MAIS" -> { page = if (page == 0) 1 else 0; buildPage() }
            "⌫" -> onKeyPress("BACKSPACE")
            "↵" -> onKeyPress("ENTER")
            " " -> onKeyPress(" ")
            else -> onKeyPress(key)
        }
    }

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()

    companion object {
        val PAGE_1 = listOf(
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            listOf("@", "#", "$", "%", "&", "-", "+", "(", ")"),
            listOf("MAIS", "*", "\"", "'", ":", ";", "!", "?", "⌫"),
            listOf("ABC", ",", " ", ".", "↵")
        )
        val PAGE_2 = listOf(
            listOf("~", "`", "|", "·", "√", "π", "÷", "×", "¶", "∆"),
            listOf("£", "¢", "€", "¥", "^", "°", "=", "{", "}"),
            listOf("MAIS", "\\", "©", "®", "™", "℅", "[", "]", "⌫"),
            listOf("ABC", "<", " ", ">", "↵")
        )
    }
}
