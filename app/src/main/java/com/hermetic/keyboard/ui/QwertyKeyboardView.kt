package com.hermetic.keyboard.ui

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hermetic.keyboard.R

/**
 * Programmatic QWERTY keyboard with functional keys.
 */
class QwertyKeyboardView(
    context: Context,
    private val onKeyPress: (String) -> Unit
) : LinearLayout(context) {

    private var isShifted = false
    private var isCaps = false
    private val letterKeys = mutableListOf<TextView>()

    init {
        orientation = VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.background))
        setPadding(dpToPx(2), dpToPx(4), dpToPx(2), dpToPx(4))
        buildKeyboard()
    }

    private fun buildKeyboard() {
        val rows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "⌫"),
            listOf("123", "🔮", "א", " ", ".", "↵")
        )

        rows.forEach { row ->
            addView(createRow(row))
        }
    }

    private fun createRow(keys: List<String>): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(52)).apply {
                topMargin = dpToPx(2)
            }

            keys.forEach { key ->
                addView(createKey(key, this))
            }
        }
    }

    private fun createKey(key: String, parent: LinearLayout): TextView {
        val isSpecial = key in listOf("SHIFT", "⌫", "123", "🔮", "א", "↵")
        val isSpace = key == " "

        val weight = when {
            isSpace -> 4f
            isSpecial -> 1.5f
            else -> 1f
        }

        return TextView(context).apply {
            val displayText = when (key) {
                "SHIFT" -> "⇧"
                " " -> "espaço"
                "123" -> "?123"
                "🔮" -> "🔮"
                "א" -> "א"
                "↵" -> "↵"
                else -> if (isShifted || isCaps) key.uppercase() else key
            }
            text = displayText
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.key_text))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isSpecial || isSpace) 16f else 20f)
            setBackgroundResource(R.drawable.key_background)
            layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, weight).apply {
                marginStart = dpToPx(2)
                marginEnd = dpToPx(2)
            }

            if (!isSpecial && !isSpace) {
                letterKeys.add(this)
            }

            setOnClickListener {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                handleKeyPress(key)
            }

            // Long press backspace for continuous delete
            if (key == "⌫") {
                setOnLongClickListener {
                    // Delete word
                    onKeyPress("DELETE_WORD")
                    true
                }
            }
        }
    }

    private fun handleKeyPress(key: String) {
        when (key) {
            "SHIFT" -> toggleShift()
            "⌫" -> onKeyPress("BACKSPACE")
            "123" -> onKeyPress("SWITCH_TO_SYMBOLS")
            "🔮" -> onKeyPress("SWITCH_TO_HERMETIC")
            "א" -> onKeyPress("SWITCH_TO_HEBREW")
            "↵" -> onKeyPress("ENTER")
            " " -> onKeyPress(" ")
            "." -> onKeyPress(".")
            else -> {
                val output = if (isShifted || isCaps) key.uppercase() else key
                onKeyPress(output)
                if (isShifted && !isCaps) {
                    isShifted = false
                    updateLetterCase()
                }
            }
        }
    }

    private fun toggleShift() {
        if (!isShifted && !isCaps) {
            isShifted = true
        } else if (isShifted && !isCaps) {
            isCaps = true
        } else {
            isShifted = false
            isCaps = false
        }
        updateLetterCase()
    }

    private fun updateLetterCase() {
        letterKeys.forEach { tv ->
            val currentText = tv.text.toString()
            tv.text = if (isShifted || isCaps) currentText.uppercase() else currentText.lowercase()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
