package com.hermetic.keyboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
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
 * High-performance QWERTY keyboard using Canvas drawing.
 *
 * Architecture:
 * - FrameLayout containing: SuggestionBar (top) + CanvasKeyboard (main) + AccentOverlay
 * - The CanvasKeyboard is a single flat View that draws all keys via onDraw(canvas)
 * - Contiguous hitboxes (no dead zones between keys)
 * - Multi-touch support via ACTION_POINTER_DOWN/UP
 * - Center-gravity algorithm for edge touches
 *
 * Layout (Samsung Galaxy A30s: 720x1560, 280dpi):
 * - Row heights: 52dp
 * - Row 1: q-p (10 keys, full width)
 * - Row 2: a-l (9 keys, indented half-key width each side)
 * - Row 3: Shift(1.5x) + z,x,c,v,b,n,m + Backspace(1.5x)
 * - Row 4: ?123(1.2x), 😀(1x), 🎙️(1x), 🔮(1x), Space(3x), .(1x), Enter(1.3x)
 */
class QwertyKeyboardView(
    context: Context,
    private val onKeyPress: (String) -> Unit
) : FrameLayout(context) {

    private var isShifted = false
    private var isCaps = false

    private lateinit var suggestionBar: SuggestionBarView
    private lateinit var canvasKeyboard: CanvasKeyboardView
    private lateinit var accentOverlay: LinearLayout
    private var currentWord = StringBuilder()

    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.background))
        buildLayout()
    }

    private fun buildLayout() {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
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
        container.addView(suggestionBar)

        // Canvas keyboard
        canvasKeyboard = CanvasKeyboardView(context) { action ->
            handleCanvasAction(action)
        }
        container.addView(canvasKeyboard)

        addView(container)

        // Accent overlay (drawn on top)
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

    private fun handleCanvasAction(action: String) {
        when {
            action == "SHIFT_TAP" -> {
                toggleShift()
                canvasKeyboard.updateShiftState(isShifted, isCaps)
            }
            action == "SHIFT_LONG" -> {
                isCaps = true; isShifted = true
                canvasKeyboard.updateShiftState(isShifted, isCaps)
            }
            action == "BACKSPACE" -> {
                onKeyPress("BACKSPACE")
                trackBackspace()
            }
            action == "BACKSPACE_WORD" -> {
                onKeyPress("BACKSPACE")
                trackBackspace()
            }
            action.startsWith("ACCENT_") -> {
                val baseKey = action.removePrefix("ACCENT_")
                showAccentRow(baseKey)
            }
            action == "SWITCH_TO_SYMBOLS" -> onKeyPress("SWITCH_TO_SYMBOLS")
            action == "SWITCH_TO_EMOJI" -> onKeyPress("SWITCH_TO_EMOJI")
            action == "SWITCH_TO_HERMETIC" -> onKeyPress("SWITCH_TO_HERMETIC")
            action == "VOICE_INPUT" -> {
                // Placeholder: show toast "Em breve"
                android.widget.Toast.makeText(context, "Em breve", android.widget.Toast.LENGTH_SHORT).show()
            }
            action == "ENTER" -> {
                onKeyPress("ENTER")
                if (currentWord.isNotEmpty()) suggestionBar.onWordCompleted(currentWord.toString())
                currentWord.clear()
            }
            action == "SPACE" -> {
                onKeyPress(" ")
                if (currentWord.isNotEmpty()) suggestionBar.onWordCompleted(currentWord.toString())
                currentWord.clear()
            }
            action == "." -> {
                onKeyPress(".")
                if (currentWord.isNotEmpty()) suggestionBar.onWordCompleted(currentWord.toString())
                currentWord.clear()
            }
            else -> {
                // Regular letter
                val output = if (isShifted || isCaps) action.uppercase() else action
                onKeyPress(output)
                currentWord.append(output)
                suggestionBar.updateSuggestions(currentWord.toString())
                if (isShifted && !isCaps) {
                    isShifted = false
                    canvasKeyboard.updateShiftState(isShifted, isCaps)
                }
            }
        }
    }

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
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
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
            isShifted && !isCaps -> isShifted = false
            isCaps -> { isCaps = false; isShifted = false }
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

// ============================================================================
// CanvasKeyboardView - Single flat View that draws all keys via Canvas
// ============================================================================

/**
 * A single flat View that paints all keyboard keys using Canvas.
 *
 * Benefits:
 * - No nested views (LinearLayouts/TextViews) for maximum performance
 * - Contiguous hitboxes with no dead zones between keys
 * - Multi-touch support via ACTION_POINTER_DOWN/UP
 * - Center-gravity algorithm for edge touches
 */
class CanvasKeyboardView(
    context: Context,
    private val onAction: (String) -> Unit
) : View(context) {

    // Key data model
    data class KeyData(
        val label: String,
        val action: String,
        val rect: RectF = RectF(),
        var isPressed: Boolean = false,
        val isSpecial: Boolean = false,
        val textSizeSp: Float = 20f
    )

    // Layout definition: rows with (label, action, weight)
    private data class KeyDef(val label: String, val action: String, val weight: Float, val isSpecial: Boolean = false)

    // Row definitions
    private val rowDefs = listOf(
        // Row 1: q-p
        listOf(
            KeyDef("q", "q", 1f), KeyDef("w", "w", 1f), KeyDef("e", "e", 1f),
            KeyDef("r", "r", 1f), KeyDef("t", "t", 1f), KeyDef("y", "y", 1f),
            KeyDef("u", "u", 1f), KeyDef("i", "i", 1f), KeyDef("o", "o", 1f),
            KeyDef("p", "p", 1f)
        ),
        // Row 2: a-l (indented)
        listOf(
            KeyDef("a", "a", 1f), KeyDef("s", "s", 1f), KeyDef("d", "d", 1f),
            KeyDef("f", "f", 1f), KeyDef("g", "g", 1f), KeyDef("h", "h", 1f),
            KeyDef("j", "j", 1f), KeyDef("k", "k", 1f), KeyDef("l", "l", 1f)
        ),
        // Row 3: Shift + z-m + Backspace
        listOf(
            KeyDef("⇧", "SHIFT", 1.5f, true),
            KeyDef("z", "z", 1f), KeyDef("x", "x", 1f), KeyDef("c", "c", 1f),
            KeyDef("v", "v", 1f), KeyDef("b", "b", 1f), KeyDef("n", "n", 1f),
            KeyDef("m", "m", 1f),
            KeyDef("⌫", "BACKSPACE", 1.5f, true)
        ),
        // Row 4: ?123, 😀, 🎙️, 🔮, Space, ., Enter
        listOf(
            KeyDef("?123", "SWITCH_TO_SYMBOLS", 1.2f, true),
            KeyDef("😀", "SWITCH_TO_EMOJI", 1f, true),
            KeyDef("🎙️", "VOICE_INPUT", 1f, true),
            KeyDef("🔮", "SWITCH_TO_HERMETIC", 1f, true),
            KeyDef("", "SPACE", 3f, true),
            KeyDef(".", ".", 1f, true),
            KeyDef("↵", "ENTER", 1.3f, true)
        )
    )

    // Flattened key list (computed after layout)
    private val keys = mutableListOf<KeyData>()

    // Paints
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.key_background)
        style = Paint.Style.FILL
    }
    private val keyBgPressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.key_background_pressed)
        style = Paint.Style.FILL
    }
    private val keyBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.key_border)
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(1).toFloat()
    }
    private val keyTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.key_text)
        textAlign = Paint.Align.CENTER
    }

    // Layout constants
    private val rowHeightDp = 52
    private val rowSpacingDp = 4
    private val keyPaddingDp = 2
    private val cornerRadiusDp = 6
    private val numRows = 4

    // Multi-touch tracking: pointerId -> keyIndex
    private val pointerKeyMap = mutableMapOf<Int, Int>()

    // Long-press handling
    private val longPressHandler = Handler(Looper.getMainLooper())
    private val longPressRunnables = mutableMapOf<Int, Runnable>()

    // Backspace repeat
    private val backspaceHandler = Handler(Looper.getMainLooper())
    private var backspaceHeld = false
    private var backspaceHoldStart = 0L
    private var backspaceWordMode = false
    private val backspaceRepeater = object : Runnable {
        override fun run() {
            if (!backspaceHeld) return
            val elapsed = System.currentTimeMillis() - backspaceHoldStart
            if (elapsed >= 1000L && !backspaceWordMode) {
                backspaceWordMode = true
            }
            onAction("BACKSPACE")
            val delay = if (backspaceWordMode) 80L else 50L
            backspaceHandler.postDelayed(this, delay)
        }
    }

    // Shift state
    private var isShifted = false
    private var isCaps = false

    init {
        isFocusable = false
        isClickable = true
    }

    fun updateShiftState(shifted: Boolean, caps: Boolean) {
        isShifted = shifted
        isCaps = caps
        // Update key labels
        keys.forEach { key ->
            if (key.action.length == 1 && key.action[0].isLetter()) {
                // This is a letter key - no direct label storage update needed,
                // we handle it in onDraw
            }
            if (key.action == "SHIFT") {
                // Update shift icon label reference in onDraw
            }
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val totalHeight = dpToPx(rowHeightDp) * numRows + dpToPx(rowSpacingDp) * (numRows + 1)
        setMeasuredDimension(width, totalHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        computeKeyRects(w, h)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun computeKeyRects(viewWidth: Int, viewHeight: Int) {
        keys.clear()
        val rowHeight = dpToPx(rowHeightDp).toFloat()
        val rowSpacing = dpToPx(rowSpacingDp).toFloat()
        val keyPadding = dpToPx(keyPaddingDp).toFloat()
        val halfKeyWidth = viewWidth.toFloat() / 20f // half of a standard key in row 1

        var y = rowSpacing

        rowDefs.forEachIndexed { rowIndex, rowKeys ->
            val totalWeight = rowKeys.sumOf { it.weight.toDouble() }.toFloat()

            // Calculate row indent for row 2
            val indent = if (rowIndex == 1) halfKeyWidth else 0f
            val availableWidth = viewWidth.toFloat() - (indent * 2f)

            var x = indent

            rowKeys.forEach { keyDef ->
                val keyWidth = (keyDef.weight / totalWeight) * availableWidth
                val rect = RectF(
                    x + keyPadding,
                    y + keyPadding,
                    x + keyWidth - keyPadding,
                    y + rowHeight - keyPadding
                )

                val textSize = if (keyDef.isSpecial) 16f else 20f

                keys.add(KeyData(
                    label = keyDef.label,
                    action = keyDef.action,
                    rect = rect,
                    isSpecial = keyDef.isSpecial,
                    textSizeSp = textSize
                ))

                x += keyWidth
            }

            y += rowHeight + rowSpacing
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cornerRadius = dpToPx(cornerRadiusDp).toFloat()

        keys.forEach { key ->
            // Background
            val bgPaint = if (key.isPressed) keyBgPressedPaint else keyBgPaint
            canvas.drawRoundRect(key.rect, cornerRadius, cornerRadius, bgPaint)

            // Border
            canvas.drawRoundRect(key.rect, cornerRadius, cornerRadius, keyBorderPaint)

            // Text
            val displayLabel = getDisplayLabel(key)
            keyTextPaint.textSize = spToPx(key.textSizeSp)
            val textX = key.rect.centerX()
            val textY = key.rect.centerY() - (keyTextPaint.descent() + keyTextPaint.ascent()) / 2f
            canvas.drawText(displayLabel, textX, textY, keyTextPaint)
        }
    }

    private fun getDisplayLabel(key: KeyData): String {
        return when {
            key.action == "SHIFT" -> when {
                isCaps -> "⇪"
                isShifted -> "⬆"
                else -> "⇧"
            }
            key.action.length == 1 && key.action[0].isLetter() -> {
                if (isShifted || isCaps) key.label.uppercase() else key.label
            }
            else -> key.label
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val keyIndex = findKeyAt(x, y)
                if (keyIndex >= 0) {
                    pointerKeyMap[pointerId] = keyIndex
                    keys[keyIndex].isPressed = true
                    invalidate()

                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                    val key = keys[keyIndex]
                    when (key.action) {
                        "BACKSPACE" -> {
                            onAction("BACKSPACE")
                            backspaceHeld = true
                            backspaceWordMode = false
                            backspaceHoldStart = System.currentTimeMillis()
                            backspaceHandler.postDelayed(backspaceRepeater, 300)
                        }
                        "SHIFT" -> {
                            // Start long-press detection for caps lock
                            val runnable = Runnable {
                                onAction("SHIFT_LONG")
                                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                longPressRunnables.remove(pointerId)
                            }
                            longPressRunnables[pointerId] = runnable
                            longPressHandler.postDelayed(runnable, 500)
                        }
                        else -> {
                            // Fire immediately for all other keys
                            fireKeyAction(key)
                            // Start long-press for accent if applicable
                            if (key.action.length == 1 && hasAccents(key.action)) {
                                val runnable = Runnable {
                                    onAction("ACCENT_${key.action}")
                                    longPressRunnables.remove(pointerId)
                                }
                                longPressRunnables[pointerId] = runnable
                                longPressHandler.postDelayed(runnable, 400)
                            }
                        }
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val keyIndex = pointerKeyMap.remove(pointerId)
                if (keyIndex != null && keyIndex < keys.size) {
                    keys[keyIndex].isPressed = false
                    invalidate()

                    val key = keys[keyIndex]
                    if (key.action == "BACKSPACE") {
                        backspaceHeld = false
                        backspaceWordMode = false
                        backspaceHandler.removeCallbacksAndMessages(null)
                    }
                    if (key.action == "SHIFT") {
                        val runnable = longPressRunnables.remove(pointerId)
                        if (runnable != null) {
                            // Long press didn't fire yet = short tap
                            longPressHandler.removeCallbacks(runnable)
                            onAction("SHIFT_TAP")
                        }
                        // If runnable is null, long press already fired
                    }
                }
                // Cancel any long-press for this pointer
                longPressRunnables.remove(pointerId)?.let {
                    longPressHandler.removeCallbacks(it)
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                // Release all pointers
                pointerKeyMap.values.forEach { idx ->
                    if (idx < keys.size) keys[idx].isPressed = false
                }
                pointerKeyMap.clear()
                longPressRunnables.clear()
                longPressHandler.removeCallbacksAndMessages(null)
                backspaceHeld = false
                backspaceWordMode = false
                backspaceHandler.removeCallbacksAndMessages(null)
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // Ignore moves (no swipe for now)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Find which key was hit using center-gravity distance algorithm.
     * Returns key index or -1 if none found.
     */
    private fun findKeyAt(touchX: Float, touchY: Float): Int {
        // First try exact hit
        keys.forEachIndexed { index, key ->
            if (key.rect.contains(touchX, touchY)) return index
        }

        // Center-gravity: find nearest key center for edge touches
        var minDist = Float.MAX_VALUE
        var nearest = -1
        keys.forEachIndexed { index, key ->
            val dx = touchX - key.rect.centerX()
            val dy = touchY - key.rect.centerY()
            val dist = dx * dx + dy * dy
            if (dist < minDist) {
                minDist = dist
                nearest = index
            }
        }

        // Only accept if within reasonable distance (1.5x key height)
        val maxDist = dpToPx(rowHeightDp) * 1.5f
        return if (minDist < maxDist * maxDist) nearest else -1
    }

    private fun fireKeyAction(key: KeyData) {
        when (key.action) {
            "SWITCH_TO_SYMBOLS" -> onAction("SWITCH_TO_SYMBOLS")
            "SWITCH_TO_EMOJI" -> onAction("SWITCH_TO_EMOJI")
            "SWITCH_TO_HERMETIC" -> onAction("SWITCH_TO_HERMETIC")
            "VOICE_INPUT" -> onAction("VOICE_INPUT")
            "SPACE" -> onAction("SPACE")
            "ENTER" -> onAction("ENTER")
            "." -> onAction(".")
            else -> {
                // Letter key
                if (key.action.length == 1) {
                    onAction(key.action)
                }
            }
        }
    }

    private fun hasAccents(key: String): Boolean =
        QwertyKeyboardView.ACCENT_MAP.containsKey(key.lowercase())

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()

    private fun spToPx(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
}
