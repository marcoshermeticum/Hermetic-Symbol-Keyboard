package com.hermetic.keyboard.ui

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
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
 * Manages creation and switching between keyboard layouts with transition animations.
 */
class KeyboardLayoutManager(
    private val ime: HermeticIME,
    private val repository: SymbolRepository,
    private val searchEngine: SearchEngine
) {

    private val scope = CoroutineScope(Dispatchers.Main)

    fun createMainKeyboardView(): View {
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

    fun createHermeticPanelView(): View {
        return HermeticPanelView(ime, repository, searchEngine) { symbol ->
            ime.commitText(symbol.symbol)
            scope.launch { repository.addRecent(symbol) }
        }
    }

    fun createHebrewKeyboardView(): View {
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

    fun createEmojiPanelView(): View {
        return EmojiPanelView(ime) { emoji ->
            ime.commitText(emoji)
        }
    }

    fun createNumberSymbolsView(): View {
        return NumberSymbolsView(ime) { output ->
            when (output) {
                "SWITCH_TO_ALPHA" -> ime.switchToMainKeyboard()
                "BACKSPACE" -> ime.deleteBackward()
                "ENTER" -> ime.commitText("\n")
                else -> ime.commitText(output)
            }
        }
    }

    fun onInputStarted(info: EditorInfo?) {}

    companion object {
        /**
         * Quick slide-up + fade-in animation for layout transitions.
         * Duration: 120ms for snappy feel.
         */
        fun applyTransitionAnimation(view: View) {
            val slideUp = TranslateAnimation(0f, 0f, 60f, 0f).apply {
                duration = 120
            }
            val fadeIn = AlphaAnimation(0.5f, 1.0f).apply {
                duration = 120
            }
            val animSet = AnimationSet(true).apply {
                addAnimation(slideUp)
                addAnimation(fadeIn)
            }
            view.startAnimation(animSet)
        }
    }
}
