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
 * QWERTY keyboard with long-press accented characters shown inline (no PopupWindow).
 * Accents appear as a horizontal row overlaying the top of the keyboard,
 * avoiding focus issues with InputMethodService.
 */
class QwertyKeyboardView(
    context: Context,
    private val onKeyPress: (String) -> Unit
) : FrameLayout(context) {

    private var isShifted = false
    private var isCaps = false
    private val letterKeys = mutableListOf<TextView>()
    private val backspaceHandler = Handler(Looper.getMainLooper())
    private val longPressHandler = Handler(Looper.getMainLooper())
    private var isBackspaceHeld = false
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
        // Main keyboard column
        keyboardLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(3), dpToPx(4), dpToPx(3), dpToPx(6))
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

        // Key rows
        val rows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "⌫"),
            listOf("123", "😀", "🔮", " ", ",", ".", "↵")
        )
        rows.forEach { row -> keyboardLayout.addView(createRow(row)) }

        addView(keyboardLayout)

        // Accent overlay (hidden by default, shown on long-press)
        accentOverlay = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(context, R.color.surface))
            setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6))
            elevation = dpToPx(6).toFloat()
            visibility = View.GONE
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(52)).apply {
                topMargin = dpToPx(38) // Position below suggestion bar
            }
        }
        addView(accentOverlay)
    }

    private fun createRow(keys: List<String>): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(54)).apply {
                topMargin = dpToPx(4)
            }
            keys.forEach { key -> addView(createKey(key)) }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createKey(key: String): TextView {
        val isSpecial = key in listOf("SHIFT", "⌫", "123", "🔮", "😀", "↵")
        val isSpace = key == " "
        val isPunctuation = key in listOf(",", ".")

        val weight = when {
            isSpace -> 3f
            isSpecial -> 1.3f
            isPunctuation -> 1f
            else -> 1f
        }

        return TextView(context).apply {
            val displayText = when (key) {
                "SHIFT" -> if (isCaps) "⇪" else "⇧"
                " " -> ""
                "123" -> "?123"
                "🔮" -> "🔮"
                "😀" -> "😀"
                "↵" -> "↵"
                "⌫" -> "⌫"
                else -> if (isShifted || isCaps) key.uppercase() else key
            }
            text = displayText
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.key_text))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, when {
                isSpace -> 12f
                isSpecial -> 17f
                isPunctuation -> 20f
                else -> 21f
            })
            setBackgroundResource(R.drawable.key_background)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight).apply {
                marginStart = dpToPx(3)
                marginEnd = dpToPx(3)
            }
            minimumHeight = dpToPx(48)
            isFocusable = false
            isFocusableInTouchMode = false
            isClickable = true
            isHapticFeedbackEnabled = true

            if (!isSpecial && !isSpace && !isPunctuation) {
                letterKeys.add(this)
            }

            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.isPressed = true
                        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        longPressTriggered = false

                        if (key == "⌫") {
                            handleKeyPress(key)
                            isBackspaceHeld = true
                            startBackspaceRepeat()
                        } else if (hasAccents(key)) {
                            longPressHandler.postDelayed({
                                longPressTriggered = true
                                showAccentRow(key)
                            }, 400)
                        } else {
                            handleKeyPress(key)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.isPressed = false
                        longPressHandler.removeCallbacksAndMessages(null)

                        if (key == "⌫") {
                            isBackspaceHeld = false
                            backspaceHandler.removeCallbacksAndMessages(null)
                        } else if (hasAccents(key) && !longPressTriggered) {
                            handleKeyPress(key)
                        }
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun hasAccents(key: String): Boolean = ACCENT_MAP.containsKey(key.lowercase())

    /**
     * Shows accented characters as an inline overlay row.
     * No PopupWindow — avoids stealing focus from InputMethodService.
     */
    private fun showAccentRow(key: String) {
        val accents = ACCENT_MAP[key.lowercase()] ?: return
        val chars = if (isShifted || isCaps) accents.map { it.uppercase() } else accents

        accentOverlay.removeAllViews()

        // Add the base character first
        val baseChar = if (isShifted || isCaps) key.uppercase() else key
        addAccentButton(baseChar)

        // Add accented variants
        chars.forEach { char -> addAccentButton(char) }

        // Add close button
        accentOverlay.addView(TextView(context).apply {
            text = "✕"
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(ContextCompat.getColor(context, R.color.on_surface))
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            setOnClickListener { hideAccentRow() }
        })

        accentOverlay.visibility = View.VISIBLE
    }

    private fun addAccentButton(char: String) {
        accentOverlay.addView(TextView(context).apply {
            text = char
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTextColor(ContextCompat.getColor(context, R.color.on_background))
            setBackgroundResource(R.drawable.key_background)
            setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dpToPx(3)
                marginEnd = dpToPx(3)
            }
            isFocusable = false
            setOnClickListener {
                onKeyPress(char)
                currentWord.append(char)
                suggestionBar.updateSuggestions(currentWord.toString())
                if (isShifted && !isCaps) { isShifted = false; updateLetterCase() }
                hideAccentRow()
            }
        })
    }

    private fun hideAccentRow() {
        accentOverlay.visibility = View.GONE
        longPressTriggered = false
    }

    private fun startBackspaceRepeat() {
        backspaceHandler.postDelayed(object : Runnable {
            override fun run() {
                if (isBackspaceHeld) {
                    onKeyPress("BACKSPACE")
                    trackBackspace()
                    backspaceHandler.postDelayed(this, 50)
                }
            }
        }, 300)
    }

    private fun handleKeyPress(key: String) {
        // Hide accent overlay if visible
        if (accentOverlay.visibility == View.VISIBLE) hideAccentRow()

        when (key) {
            "SHIFT" -> toggleShift()
            "⌫" -> { onKeyPress("BACKSPACE"); trackBackspace() }
            "123" -> onKeyPress("SWITCH_TO_SYMBOLS")
            "🔮" -> onKeyPress("SWITCH_TO_HERMETIC")
            "😀" -> onKeyPress("SWITCH_TO_EMOJI")
            "↵" -> {
                onKeyPress("ENTER")
                if (currentWord.isNotEmpty()) suggestionBar.onWordCompleted(currentWord.toString())
                currentWord.clear(); suggestionBar.clear()
            }
            " " -> {
                onKeyPress(" ")
                if (currentWord.isNotEmpty()) suggestionBar.onWordCompleted(currentWord.toString())
                currentWord.clear(); suggestionBar.clear()
            }
            ",", "." -> {
                onKeyPress(key)
                if (currentWord.isNotEmpty()) suggestionBar.onWordCompleted(currentWord.toString())
                currentWord.clear(); suggestionBar.clear()
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
            isShifted && !isCaps -> { isCaps = true; isShifted = true }
            else -> { isShifted = false; isCaps = false }
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
        /** Long-press accent map for Portuguese and common Latin languages */
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
