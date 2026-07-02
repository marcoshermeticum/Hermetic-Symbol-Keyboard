package com.hermetic.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.hermetic.keyboard.symbols.data.SymbolDatabase
import com.hermetic.keyboard.symbols.data.SymbolDataProvider
import com.hermetic.keyboard.symbols.repository.SymbolRepository
import com.hermetic.keyboard.symbols.search.SearchEngine
import com.hermetic.keyboard.ui.KeyboardLayoutManager

/**
 * Main InputMethodService for the Hermetic Symbol Keyboard.
 */
class HermeticIME : InputMethodService() {

    private lateinit var repository: SymbolRepository
    private lateinit var searchEngine: SearchEngine
    private lateinit var layoutManager: KeyboardLayoutManager

    private var currentView: View? = null

    override fun onCreate() {
        super.onCreate()
        val db = SymbolDatabase.getInstance(this)
        val dataProvider = SymbolDataProvider(this)
        repository = SymbolRepository(dataProvider, db.favoriteDao(), db.recentDao())
        searchEngine = SearchEngine()
        layoutManager = KeyboardLayoutManager(this, repository, searchEngine)
    }

    override fun onCreateInputView(): View {
        val view = layoutManager.createMainKeyboardView()
        currentView = view
        return view
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
        val view = layoutManager.createHermeticPanelView()
        switchView(view)
    }

    fun switchToHebrewKeyboard() {
        val view = layoutManager.createHebrewKeyboardView()
        switchView(view)
    }

    fun switchToMainKeyboard() {
        val view = layoutManager.createMainKeyboardView()
        switchView(view)
    }

    fun switchToEmojiPanel() {
        val view = layoutManager.createEmojiPanelView()
        switchView(view)
    }

    private fun switchView(view: View) {
        setInputView(view)
        currentView = view
        KeyboardLayoutManager.applyTransitionAnimation(view)
    }
}
