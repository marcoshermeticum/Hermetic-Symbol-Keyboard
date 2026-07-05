package com.hermetic.keyboard.ui

import android.animation.ObjectAnimator
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.hermetic.keyboard.R
import com.hermetic.keyboard.ime.HermeticIME
import com.hermetic.keyboard.symbols.repository.SymbolRepository
import com.hermetic.keyboard.symbols.search.SearchEngine
import com.hermetic.keyboard.ui.hebrew.HebrewKeyboardView
import com.hermetic.keyboard.ui.panel.EmojiPanelView
import com.hermetic.keyboard.ui.panel.HermeticPanelView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manages creation and switching between keyboard layouts.
 *
 * Architecture:
 * - Pre-instantiates all 5 views in a ViewCache on init
 * - Stores all views in a FrameLayout with background #090908
 * - Switches via visibility toggle (VISIBLE/GONE) + 80ms alpha animation
 * - No removeView/addView (prevents remeasure/relayout)
 */
class KeyboardLayoutManager(
    private val ime: HermeticIME,
    private val repository: SymbolRepository,
    private val searchEngine: SearchEngine
) {

    private val scope = CoroutineScope(Dispatchers.Main)

    // The container FrameLayout holding all pre-instantiated views
    lateinit var container: FrameLayout
        private set

    // View cache - all 5 keyboard views
    private lateinit var mainKeyboardView: View
    private lateinit var hermeticPanelView: View
    private lateinit var hebrewKeyboardView: View
    private lateinit var emojiPanelView: View
    private lateinit var numberSymbolsView: View

    private var currentView: View? = null

    /**
     * Pre-instantiates all views and stores them in a FrameLayout container.
     * Call this in HermeticIME.onCreate() or on first onCreateInputView().
     */
    fun initViewCache() {
        container = FrameLayout(ime).apply {
            setBackgroundColor(ContextCompat.getColor(ime, R.color.background))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Create all views
        mainKeyboardView = createMainKeyboardViewInternal()
        hermeticPanelView = createHermeticPanelViewInternal()
        hebrewKeyboardView = createHebrewKeyboardViewInternal()
        emojiPanelView = createEmojiPanelViewInternal()
        numberSymbolsView = createNumberSymbolsViewInternal()

        // Add all to container with GONE visibility (except main)
        container.addView(mainKeyboardView)
        container.addView(hermeticPanelView.apply { visibility = View.GONE })
        container.addView(hebrewKeyboardView.apply { visibility = View.GONE })
        container.addView(emojiPanelView.apply { visibility = View.GONE })
        container.addView(numberSymbolsView.apply { visibility = View.GONE })

        currentView = mainKeyboardView
    }

    /**
     * Returns the pre-built FrameLayout container.
     */
    fun getContainerView(): View {
        if (!::container.isInitialized) {
            initViewCache()
        }
        return container
    }

    // --- Switching methods (visibility toggle + alpha animation) ---

    fun switchToMain() {
        switchTo(mainKeyboardView)
    }

    fun switchToHermetic() {
        switchTo(hermeticPanelView)
    }

    fun switchToHebrew() {
        switchTo(hebrewKeyboardView)
    }

    fun switchToEmoji() {
        switchTo(emojiPanelView)
    }

    fun switchToNumberSymbols() {
        switchTo(numberSymbolsView)
    }

    private fun switchTo(target: View) {
        if (currentView == target) return

        // Hide current
        currentView?.visibility = View.GONE

        // Show target with alpha animation
        target.visibility = View.VISIBLE
        target.alpha = 0.5f
        ObjectAnimator.ofFloat(target, "alpha", 0.5f, 1.0f).apply {
            duration = 80
            start()
        }

        currentView = target
    }

    // --- Internal view creation ---

    private fun createMainKeyboardViewInternal(): View {
        return QwertyKeyboardView(ime) { output ->
            when {
                output == "BACKSPACE" -> ime.deleteBackward()
                output.startsWith("DELETE_WORD_") -> {
                    val count = output.removePrefix("DELETE_WORD_").toIntOrNull() ?: 0
                    if (count > 0) ime.deleteBackward(count)
                }
                output == "ENTER" -> ime.commitText("\n")
                output == "SWITCH_TO_SYMBOLS" -> ime.switchToNumberSymbols()
                output == "SWITCH_TO_HERMETIC" -> ime.switchToHermeticPanel()
                output == "SWITCH_TO_HEBREW" -> ime.switchToHebrewKeyboard()
                output == "SWITCH_TO_EMOJI" -> ime.switchToEmojiPanel()
                else -> ime.commitText(output)
            }
        }
    }

    private fun createHermeticPanelViewInternal(): View {
        return HermeticPanelView(ime, repository, searchEngine) { symbol ->
            ime.commitText(symbol.symbol)
            scope.launch { repository.addRecent(symbol) }
        }
    }

    private fun createHebrewKeyboardViewInternal(): View {
        return HebrewKeyboardView(ime) { output ->
            when (output) {
                "BACKSPACE" -> ime.deleteBackward()
                "SWITCH_TO_HERMETIC" -> ime.switchToHermeticPanel()
                "SWITCH_TO_ALPHA" -> ime.switchToMainKeyboard()
                " " -> ime.commitText(" ")
                "\n" -> ime.commitText("\n")
                else -> ime.commitText(output)
            }
        }
    }

    private fun createEmojiPanelViewInternal(): View {
        return EmojiPanelView(ime) { emoji ->
            ime.commitText(emoji)
        }
    }

    private fun createNumberSymbolsViewInternal(): View {
        return NumberSymbolsView(ime) { output ->
            when (output) {
                "SWITCH_TO_ALPHA" -> ime.switchToMainKeyboard()
                "BACKSPACE" -> ime.deleteBackward()
                "ENTER" -> ime.commitText("\n")
                else -> ime.commitText(output)
            }
        }
    }

    // --- Legacy API compatibility ---

    fun createMainKeyboardView(): View = getContainerView()

    fun createHermeticPanelView(): View {
        switchToHermetic()
        return container
    }

    fun createHebrewKeyboardView(): View {
        switchToHebrew()
        return container
    }

    fun createEmojiPanelView(): View {
        switchToEmoji()
        return container
    }

    fun createNumberSymbolsView(): View {
        switchToNumberSymbols()
        return container
    }

    @Suppress("UNUSED_PARAMETER")
    fun onInputStarted(info: EditorInfo?) {}

    companion object {
        /**
         * Quick alpha animation for layout transitions.
         * Duration: 80ms for snappy feel.
         */
        fun applyTransitionAnimation(view: View) {
            ObjectAnimator.ofFloat(view, "alpha", 0.5f, 1.0f).apply {
                duration = 80
                start()
            }
        }
    }
}
