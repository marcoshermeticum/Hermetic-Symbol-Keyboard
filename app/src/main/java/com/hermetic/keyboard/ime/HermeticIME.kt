package com.hermetic.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.hermetic.keyboard.symbols.data.SymbolDatabase
import com.hermetic.keyboard.symbols.data.SymbolDataProvider
import com.hermetic.keyboard.symbols.repository.SymbolRepository
import com.hermetic.keyboard.symbols.search.SearchEngine
import com.hermetic.keyboard.ui.KeyboardLayoutManager

/**
 * Main InputMethodService for the Hermetic Symbol Keyboard.
 *
 * Architecture:
 * - onCreateInputView returns a FrameLayout container with all views pre-loaded
 * - switchTo* methods just toggle visibility (no removeView/addView)
 * - Uses KeyboardLayoutManager's ViewCache for instant switching
 */
class HermeticIME : InputMethodService() {

    private lateinit var repository: SymbolRepository
    private lateinit var searchEngine: SearchEngine
    private lateinit var layoutManager: KeyboardLayoutManager

    override fun onCreate() {
        super.onCreate()
        val db = SymbolDatabase.getInstance(this)
        val dataProvider = SymbolDataProvider(this)
        repository = SymbolRepository(dataProvider, db.favoriteDao(), db.recentDao())
        searchEngine = SearchEngine()
        layoutManager = KeyboardLayoutManager(this, repository, searchEngine)

        // Pre-instantiate all views in the ViewCache
        layoutManager.initViewCache()
    }

    override fun onCreateInputView(): View {
        // Return the FrameLayout container with all views pre-loaded
        return layoutManager.getContainerView()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        layoutManager.onInputStarted(info)
    }

    fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    fun sendKeyEvent(event: android.view.KeyEvent) {
        currentInputConnection?.sendKeyEvent(event)
    }

    fun deleteBackward(count: Int = 1) {
        currentInputConnection?.deleteSurroundingText(count, 0)
    }

    fun switchToHermeticPanel() {
        layoutManager.switchToHermetic()
    }

    fun switchToHebrewKeyboard() {
        layoutManager.switchToHebrew()
    }

    fun switchToMainKeyboard() {
        layoutManager.switchToMain()
    }

    fun switchToEmojiPanel() {
        layoutManager.switchToEmoji()
    }

    fun switchToNumberSymbols() {
        layoutManager.switchToNumberSymbols()
    }

    fun switchToVoiceInput() {
        // Placeholder: mic icon present but shows toast "Em breve" for now
        Toast.makeText(this, "Em breve", Toast.LENGTH_SHORT).show()
    }
}
