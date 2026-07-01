package com.hermetic.keyboard.ui

import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import com.hermetic.keyboard.R
import com.hermetic.keyboard.ime.HermeticIME
import com.hermetic.keyboard.symbols.repository.SymbolRepository
import com.hermetic.keyboard.symbols.search.SearchEngine
import com.hermetic.keyboard.ui.hebrew.HebrewKeyboardView
import com.hermetic.keyboard.ui.panel.HermeticPanelView

/**
 * Manages creation and switching between different keyboard layout views.
 */
class KeyboardLayoutManager(
    private val ime: HermeticIME,
    private val repository: SymbolRepository,
    private val searchEngine: SearchEngine
) {

    /**
     * Creates the main QWERTY keyboard view.
     */
    fun createMainKeyboardView(): View {
        val view = LayoutInflater.from(ime).inflate(R.layout.keyboard_main, null)
        // TODO: Wire up QWERTY key listeners
        return view
    }

    /**
     * Creates the hermetic symbols panel view.
     */
    fun createHermeticPanelView(): View {
        return HermeticPanelView(ime, repository, searchEngine) { symbol ->
            ime.commitText(symbol.symbol)
        }
    }

    /**
     * Creates the Hebrew transliterated keyboard view.
     */
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

    fun onInputStarted(info: EditorInfo?) {
        // Handle input type specifics if needed
    }
}
