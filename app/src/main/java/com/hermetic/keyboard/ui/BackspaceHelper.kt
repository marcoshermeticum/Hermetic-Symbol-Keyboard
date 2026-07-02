package com.hermetic.keyboard.ui

import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View

/**
 * Reusable long-press backspace handler.
 * Attach to any backspace key view for consistent repeat-delete behavior.
 */
object BackspaceHelper {

    /**
     * Sets up long-press repeat on a backspace view.
     * Fires immediately on ACTION_DOWN, then repeats at 50ms after 300ms hold.
     */
    fun attach(view: View, onDelete: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        var isHeld = false

        val repeater = object : Runnable {
            override fun run() {
                if (isHeld) {
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
                    handler.postDelayed(repeater, 300)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false
                    isHeld = false
                    handler.removeCallbacksAndMessages(null)
                    true
                }
                else -> false
            }
        }
    }
}
