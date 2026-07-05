package com.hermetic.keyboard.ui

import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputConnection

/**
 * Reusable long-press backspace handler.
 * Attach to any backspace key view for consistent repeat-delete behavior.
 *
 * Behavior:
 * - Fires immediately on ACTION_DOWN
 * - After 300ms hold: repeats at 50ms interval (char-by-char)
 * - After 1000ms hold: switches to word-deletion mode (deletes entire words)
 */
object BackspaceHelper {

    /**
     * Sets up long-press repeat on a backspace view.
     * Fires immediately on ACTION_DOWN, then repeats at 50ms after 300ms hold.
     * After 1 second of holding, switches to word-deletion mode.
     */
    fun attach(view: View, onDelete: () -> Unit) {
        attach(view, null, onDelete)
    }

    /**
     * Sets up long-press repeat with word-deletion support.
     * If inputConnection is provided, after 1s hold it deletes whole words.
     *
     * @param view The backspace key view
     * @param inputConnectionProvider Provider for InputConnection (nullable for backward compat)
     * @param onDelete Called for each single-char delete
     */
    fun attach(
        view: View,
        inputConnectionProvider: (() -> InputConnection?)?,
        onDelete: () -> Unit
    ) {
        val handler = Handler(Looper.getMainLooper())
        var isHeld = false
        var holdStartTime = 0L
        var wordDeleteMode = false

        val repeater = object : Runnable {
            override fun run() {
                if (!isHeld) return

                val elapsed = System.currentTimeMillis() - holdStartTime

                if (elapsed >= 1000L && inputConnectionProvider != null && !wordDeleteMode) {
                    // Switch to word-deletion mode
                    wordDeleteMode = true
                }

                if (wordDeleteMode && inputConnectionProvider != null) {
                    val ic = inputConnectionProvider()
                    if (ic != null) {
                        deleteWord(ic)
                    } else {
                        onDelete()
                    }
                    handler.postDelayed(this, 80)
                } else {
                    onDelete()
                    handler.postDelayed(this, 50)
                }
            }
        }

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.isPressed = true
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onDelete()
                    isHeld = true
                    wordDeleteMode = false
                    holdStartTime = System.currentTimeMillis()
                    handler.postDelayed(repeater, 300)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false
                    isHeld = false
                    wordDeleteMode = false
                    handler.removeCallbacksAndMessages(null)
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Deletes one word (back to previous space or start of text).
     */
    private fun deleteWord(ic: InputConnection) {
        val textBefore = ic.getTextBeforeCursor(50, 0)?.toString() ?: return
        if (textBefore.isEmpty()) return

        // Trim trailing spaces first
        val trimmed = textBefore.trimEnd()
        val trailingSpaces = textBefore.length - trimmed.length

        if (trimmed.isEmpty()) {
            // Only spaces, delete them all
            ic.deleteSurroundingText(trailingSpaces, 0)
            return
        }

        // Find the last space in trimmed text
        val lastSpace = trimmed.lastIndexOf(' ')
        val charsToDelete = if (lastSpace >= 0) {
            // Delete from current position back to after the last space
            (trimmed.length - lastSpace - 1) + trailingSpaces
        } else {
            // No space found, delete everything
            textBefore.length
        }

        if (charsToDelete > 0) {
            ic.deleteSurroundingText(charsToDelete, 0)
        }
    }
}
