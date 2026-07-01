package com.hermetic.keyboard.ui.hebrew

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.hermetic.keyboard.R
import com.hermetic.keyboard.symbols.model.HebrewKey

/**
 * Custom keyboard view that displays Hebrew letter names in transliterated form.
 * Each key shows the name (e.g., "Aleph") but outputs the Hebrew character (e.g., "א").
 *
 * This allows users unfamiliar with the Hebrew keyboard layout to type
 * Hebrew characters by recognizing their transliterated names.
 */
class HebrewKeyboardView(
    context: Context,
    private val onKeyPress: (String) -> Unit
) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.background))
        setupKeyboard()
    }

    private fun setupKeyboard() {
        val rows = getHebrewKeyRows()
        rows.forEach { row ->
            addView(createRow(row))
        }
    }

    private fun createRow(keys: List<HebrewKey>): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = dpToPx(2)
                bottomMargin = dpToPx(2)
            }

            keys.forEach { key ->
                addView(createKeyView(key))
            }
        }
    }

    private fun createKeyView(key: HebrewKey): TextView {
        val keyWidth = if (key.isSpecialKey) dpToPx(56) else dpToPx(42)
        val keyHeight = dpToPx(48)

        return TextView(context).apply {
            text = key.label
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.key_text))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (key.isSpecialKey) 14f else 11f)
            setBackgroundResource(R.drawable.key_background)
            layoutParams = LinearLayout.LayoutParams(keyWidth, keyHeight).apply {
                marginStart = dpToPx(2)
                marginEnd = dpToPx(2)
            }

            // Show Hebrew character as hint in top-right corner if not a special key
            if (!key.isSpecialKey && key.output.length == 1) {
                // Could use compound drawables or paint overlay for the hint
                // For now, the label already conveys the intent
            }

            setOnClickListener {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onKeyPress(key.output)
            }

            setOnLongClickListener {
                if (!key.isSpecialKey) {
                    // Show tooltip with full info
                    val info = "${key.hebrewName} (${key.output}) — Gematria: ${key.gematriaValue}"
                    Toast.makeText(context, info, Toast.LENGTH_SHORT).show()
                }
                true
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    companion object {
        /**
         * Returns the Hebrew keyboard rows with transliterated labels.
         * Each key maps a Latin-script name to the Hebrew Unicode character.
         */
        fun getHebrewKeyRows(): List<List<HebrewKey>> = listOf(
            // Row 1
            listOf(
                HebrewKey("Qof", "ק", "Qof", 100),
                HebrewKey("Resh", "ר", "Resh", 200),
                HebrewKey("Aleph", "א", "Aleph", 1),
                HebrewKey("Tet", "ט", "Tet", 9),
                HebrewKey("Vav", "ו", "Vav", 6),
                HebrewKey("Nun·", "ן", "Nun Sofit", 700, isSofit = true),
                HebrewKey("Mem·", "ם", "Mem Sofit", 600, isSofit = true),
                HebrewKey("Pe", "פ", "Pe", 80)
            ),
            // Row 2
            listOf(
                HebrewKey("Shin", "ש", "Shin", 300),
                HebrewKey("Dalet", "ד", "Dalet", 4),
                HebrewKey("Gimel", "ג", "Gimel", 3),
                HebrewKey("Kaf", "כ", "Kaf", 20),
                HebrewKey("Ayin", "ע", "Ayin", 70),
                HebrewKey("Yod", "י", "Yod", 10),
                HebrewKey("Chet", "ח", "Chet", 8),
                HebrewKey("Lamed", "ל", "Lamed", 30),
                HebrewKey("Kaf·", "ך", "Kaf Sofit", 500, isSofit = true),
                HebrewKey("Pe·", "ף", "Pe Sofit", 800, isSofit = true)
            ),
            // Row 3
            listOf(
                HebrewKey("Zayin", "ז", "Zayin", 7),
                HebrewKey("Samekh", "ס", "Samekh", 60),
                HebrewKey("Bet", "ב", "Bet", 2),
                HebrewKey("He", "ה", "He", 5),
                HebrewKey("Nun", "נ", "Nun", 50),
                HebrewKey("Mem", "מ", "Mem", 40),
                HebrewKey("Tsade", "צ", "Tsade", 90),
                HebrewKey("Tsade·", "ץ", "Tsade Sofit", 900, isSofit = true),
                HebrewKey("Tav", "ת", "Tav", 400)
            ),
            // Row 4 - Function keys
            listOf(
                HebrewKey("⌫", "BACKSPACE", "Backspace", 0, isSpecialKey = true),
                HebrewKey("🔮", "SWITCH_TO_HERMETIC", "Hermetic", 0, isSpecialKey = true),
                HebrewKey("  ␣  ", " ", "Space", 0, isSpecialKey = true),
                HebrewKey("↵", "\n", "Enter", 0, isSpecialKey = true),
                HebrewKey("ABC", "SWITCH_TO_ALPHA", "Alpha", 0, isSpecialKey = true)
            )
        )
    }
}
