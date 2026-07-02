package com.hermetic.keyboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hermetic.keyboard.R

/**
 * Anatomic QWERTY keyboard layout:
 * - Row 2 (a-l) offset inward for natural finger reach
 * - Row 3 (z-m) narrower, shift/backspace wider
 * - Long-press shift = caps lock
 * - Long-press on accentable keys shows inline accent row
 */
class QwertyKeyboardView(
    context: Context,
    private val onKeyPress: (String) -> Unit
) : FrameLayout(context) {

    private var isShifted = false
    private var isCaps = false
    private val letterKeys = mutableListOf<TextView>()
    private val longPressHandler = Handler(Looper.getMainLooper())
    private var longPressTriggered = false

    private lateinit var suggestionBar: SuggestionBarView
    private lateinit var keyboardLayout: LinearLayout
    private lateinit var accentOverlay: LinearLayout
    private var currentWord = StringBuilder()

    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.background))
        buildKeyboard()
    }

    private fun buildKeyboard() {
        keyboardLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(2), dpToPx(4), dpToPx(2), dpToPx(6))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        // Suggestion bar
        suggestionBar = SuggestionBarView(context) { suggestion ->
            val wordLen = currentWord.length
            if (wordLen > 0) onKeyPress("DELETE_WORD_$wordLen")
            onKeyPress(suggestion + " ")
            suggestionBar.onWordCompleted(suggestion)
            currentWord.clear()
            suggestionBar.clear()
        }
        keyboardLayout.addView(suggestionBar)

        // Row 1: q w e r t y u i o p (full width)
        keyboardLayout.addView(createRow(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            indent = 0f
        ))
        // Row 2: a s d f g h j k l (indented ~half key for anatomic feel)
        keyboardLayout.addView(createRow(
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            indent = 0.5f
        ))
        // Row 3: SHIFT z x c v b n m ⌫
        keyboardLayout.addView(createFunctionalRow())
        // Row 4: 123 😀 🔮 [space] , . ↵
        keyboardLayout.addView(createBottomRow())

        addView(keyboardLayout)

        // Accent overlay
        accentOverlay = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(context, R.color.surface))
            setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6))
            elevation = dpToPx(6).toFloat()
            visibility = View.GONE
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(50)).apply {
                topMargin = dpToPx(38)
            }
        }
        addView(accentOverlay)
    }

    private fun createRow(keys: List<String>, indent: Float): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(52)).apply {
                topMargin = dpToPx(4)
            }
            // Indent for anatomic offset
            if (indent > 0f) {
                setPadding(dpToPx((indent * 16).toInt()), 0, dpToPx((indent * 16).toInt()), 0)
            }
            keys.forEach { key -> addView(createLetterKey(key)) }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createFunctionalRow(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(52)).apply {
                topMargin = dpToPx(4)
            }

            // SHIFT key (wider, with long-press = caps lock)
            addView(createShiftKey())

            // Letter keys z-m
            listOf("z", "x", "c", "v", "b", "n", "m").forEach { key ->
                addView(createLetterKey(key))
            }

            // Backspace (wider, with long-press repeat)
            addView(createBackspaceKey())
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createShiftKey(): TextView {
        return TextView(context).apply {
            text = "⇧"
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.key_text))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setBackgroundResource(R.drawable.key_background)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.6f).apply {
                marginStart = dpToPx(3); marginEnd = dpToPx(3)
            }
            isFocusable = false; isClickable = true

            val shiftLongPressHandler = Handler(Looper.getMainLooper())
            var shiftLongPressed = false

            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.isPressed = true
                        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        shiftLongPressed = false
                        // Long-press = caps lock
                        shiftLongPressHandler.postDelayed({
                            shiftLongPressed = true
                            isCaps = true; isShifted = true
                            updateLetterCase()
                            (v as TextView).text = "⇪"
                            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        }, 500)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        v.isPressed = false
                        shiftLongPressHandler.removeCallbacksAndMessages(null)
                        if (!shiftLongPressed) {
                            // Short tap: toggle shift
                            toggleShift()
                            (v as TextView).text = if (isCaps) "⇪" else if (isShifted) "⬆" else "⇧"
                        }
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        v.isPressed = false
                        shiftLongPressHandler.removeCallbacksAndMessages(null)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun createBackspaceKey(): TextView {
        val tv = TextView(context).apply {
            text = "⌫"
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.key_text))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setBackgroundResource(R.drawable.key_background)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.6f).apply {
                marginStart = dpToPx(3); marginEnd = dpToPx(3)
            }
            isFocusable = false; isClickable = true
        }
        BackspaceHelper.attach(tv) {
            onKeyPress("BACKSPACE")
            trackBackspace()
        }
        return tv
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createBottomRow(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(52)).apply {
                topMargin = dpToPx(4)
            }

            // ?123
            addView(makeSpecialKey("?123", 1.3f) { onKeyPress("SWITCH_TO_SYMBOLS") })
            // 😀
            addView(makeSpecialKey("😀", 1f) { onKeyPress("SWITCH_TO_EMOJI") })
            // 🔮
            addView(makeSpecialKey("🔮", 1f) { onKeyPress("SWITCH_TO_HERMETIC") })
            // Space
            addView(makeSpecialKey("", 3.5f) { handleKeyPress(" ") })
            // Comma
            addView(makeSpecialKey(",", 0.8f) { handleKeyPress(",") })
            // Period
            addView(makeSpecialKey(".", 0.8f) { handleKeyPress(".") })
            // Enter
            addView(makeSpecialKey("↵", 1.3f) { handleKeyPress("↵") })
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun makeSpecialKey(label: String, weight: Float, action: () -> Unit): TextView {
        return TextView(context).apply {
            text = label
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.key_text))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            setBackgroundResource(R.drawable.key_background)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight).apply {
                marginStart = dpToPx(3); marginEnd = dpToPx(3)
            }
            isFocusable = false; isClickable = true
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.isPressed = true
                        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        action(); true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { v.isPressed = false; true }
                    else -> false
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createLetterKey(key: String): TextView {
        return TextView(context).apply {
            text = if (isShifted || isCaps) key.uppercase() else key
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.key_text))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 21f)
            setBackgroundResource(R.drawable.key_background)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                marginStart = dpToPx(3); marginEnd = dpToPx(3)
            }
            minimumHeight = dpToPx(48)
            isFocusable = false; isFocusableInTouchMode = false
            isClickable = true; isHapticFeedbackEnabled = true

            letterKeys.add(this)

            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.isPressed = true
                        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        longPressTriggered = false
                        // Always fire immediately
                        handleKeyPress(key)
                        // Start long-press for accents if available
                        if (hasAccents(key)) {
                            longPressHandler.postDelayed({
                                longPressTriggered = true
                                showAccentRow(key)
                            }, 400)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.isPressed = false
                        longPressHandler.removeCallbacksAndMessages(null)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun hasAccents(key: String): Boolean = ACCENT_MAP.containsKey(key.lowercase())

    private fun showAccentRow(key: String) {
        val accents = ACCENT_MAP[key.lowercase()] ?: return
        val chars = if (isShifted || isCaps) accents.map { it.uppercase() } else accents
        accentOverlay.removeAllViews()

        chars.forEach { char ->
            accentOverlay.addView(TextView(context).apply {
                text = char
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                setTextColor(ContextCompat.getColor(context, R.color.on_background))
                setBackgroundResource(R.drawable.key_background)
                setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    marginStart = dpToPx(3); marginEnd = dpToPx(3)
                }
                isFocusable = false
                setOnClickListener {
                    // Replace the base char with the accented one
                    onKeyPress("BACKSPACE")
                    if (currentWord.isNotEmpty()) currentWord.deleteCharAt(currentWord.length - 1)
                    onKeyPress(char)
                    currentWord.append(char)
                    suggestionBar.updateSuggestions(currentWord.toString())
                    hideAccentRow()
                }
            })
        }

        accentOverlay.addView(TextView(context).apply {
            text = "✕"
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(ContextCompat.getColor(context, R.color.on_surface))
            setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8))
            isFocusable = false
            setOnClickListener { hideAccentRow() }
        })

        accentOverlay.visibility = View.VISIBLE
    }

    private fun hideAccentRow() {
        accentOverlay.visibility = View.GONE
        longPressTriggered = false
    }

    private fun handleKeyPress(key: String) {
        if (accentOverlay.visibility == View.VISIBLE) hideAccentRow()

        when (key) {
            "↵" -> {
                onKeyPress("ENTER")
                if (currentWord.isNotEmpty()) suggestionBar.onWordCompleted(currentWord.toString())
                currentWord.clear()
            }
            " " -> {
                onKeyPress(" ")
                if (currentWord.isNotEmpty()) suggestionBar.onWordCompleted(currentWord.toString())
                currentWord.clear()
            }
            ",", "." -> {
                onKeyPress(key)
                if (currentWord.isNotEmpty()) suggestionBar.onWordCompleted(currentWord.toString())
                currentWord.clear()
            }
            else -> {
                val output = if (isShifted || isCaps) key.uppercase() else key
                onKeyPress(output)
                currentWord.append(output)
                suggestionBar.updateSuggestions(currentWord.toString())
                if (isShifted && !isCaps) { isShifted = false; updateLetterCase() }
            }
        }
    }

    private fun trackBackspace() {
        if (currentWord.isNotEmpty()) {
            currentWord.deleteCharAt(currentWord.length - 1)
            if (currentWord.isNotEmpty()) suggestionBar.updateSuggestions(currentWord.toString())
            else suggestionBar.clear()
        }
    }

    private fun toggleShift() {
        when {
            !isShifted && !isCaps -> isShifted = true
            isShifted && !isCaps -> { isShifted = false }
            isCaps -> { isCaps = false; isShifted = false }
        }
        updateLetterCase()
    }

    private fun updateLetterCase() {
        letterKeys.forEach { tv ->
            tv.text = if (isShifted || isCaps) tv.text.toString().uppercase() else tv.text.toString().lowercase()
        }
    }

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()

    companion object {
        val ACCENT_MAP = mapOf(
            "a" to listOf("á", "à", "â", "ã", "ä"),
            "e" to listOf("é", "è", "ê", "ë"),
            "i" to listOf("í", "ì", "î", "ï"),
            "o" to listOf("ó", "ò", "ô", "õ", "ö"),
            "u" to listOf("ú", "ù", "û", "ü"),
            "c" to listOf("ç"),
            "n" to listOf("ñ"),
            "s" to listOf("ß", "š"),
            "y" to listOf("ý", "ÿ")
        )
    }
}
