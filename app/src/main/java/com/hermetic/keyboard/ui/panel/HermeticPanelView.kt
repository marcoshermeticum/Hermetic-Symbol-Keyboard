package com.hermetic.keyboard.ui.panel

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hermetic.keyboard.R
import com.hermetic.keyboard.symbols.model.Symbol
import com.hermetic.keyboard.symbols.model.SymbolCategory
import com.hermetic.keyboard.symbols.repository.SymbolRepository
import com.hermetic.keyboard.symbols.search.SearchEngine

/**
 * Panel view for hermetic symbols with PT-BR category labels.
 * Search uses a simple inline text field that doesn't require system keyboard.
 */
class HermeticPanelView(
    context: Context,
    private val repository: SymbolRepository,
    private val searchEngine: SearchEngine,
    private val onSymbolSelected: (Symbol) -> Unit
) : LinearLayout(context) {

    private var currentCategoryId: String? = null
    private lateinit var symbolGrid: RecyclerView
    private lateinit var categoryBar: LinearLayout
    private lateinit var searchField: EditText
    private var searchText = StringBuilder()

    init {
        orientation = VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.background))
        setupPanel()
    }

    private fun setupPanel() {
        addView(createSearchRow())
        addView(createCategoryBar())
        addView(createSymbolGrid())
        addView(createBottomBar())

        val categories = repository.getCategories()
        if (categories.isNotEmpty()) {
            selectCategory(categories.first().id)
        }
    }

    /**
     * Search row with a text display and inline mini-keyboard for search.
     * Since we ARE the keyboard, we can't open another keyboard for the EditText.
     * Instead we show the search text and let the bottom bar handle input.
     */
    private fun createSearchRow(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(40))
            setBackgroundColor(ContextCompat.getColor(context, R.color.surface))
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))

            // Search icon
            addView(TextView(context).apply {
                text = "🔍"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setPadding(dpToPx(4), 0, dpToPx(8), 0)
            })

            // Search text display (not a real EditText to avoid focus issues)
            searchField = EditText(context).apply {
                hint = "Buscar símbolos..."
                setHintTextColor(ContextCompat.getColor(context, R.color.on_surface))
                setTextColor(ContextCompat.getColor(context, R.color.on_background))
                setBackgroundColor(ContextCompat.getColor(context, R.color.key_background))
                setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                isSingleLine = true
                layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
                // CRITICAL: prevent focus which would close the IME
                isFocusable = false
                isFocusableInTouchMode = false
                inputType = InputType.TYPE_NULL
                // Use as display only — search keys are in bottom bar
            }
            addView(searchField)

            // Clear button
            addView(TextView(context).apply {
                text = "✕"
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, R.color.on_surface))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setPadding(dpToPx(8), 0, dpToPx(4), 0)
                setOnClickListener {
                    searchText.clear()
                    searchField.setText("")
                    currentCategoryId?.let { selectCategory(it) }
                }
            })
        }
    }

    private fun createCategoryBar(): HorizontalScrollView {
        val scrollView = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        categoryBar = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
        }

        val categories = repository.getCategories()
        categories.forEach { category ->
            categoryBar.addView(createCategoryTab(category))
        }

        scrollView.addView(categoryBar)
        return scrollView
    }

    private fun createCategoryTab(category: SymbolCategory): TextView {
        // Portuguese category labels
        val label = CATEGORY_LABELS_PT[category.id] ?: category.name
        return TextView(context).apply {
            text = label
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTextColor(ContextCompat.getColor(context, R.color.on_background))
            setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8))
            setBackgroundColor(ContextCompat.getColor(context, R.color.category_unselected))
            tag = category.id
            layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginEnd = dpToPx(4)
            }
            setOnClickListener { selectCategory(category.id) }
        }
    }

    private fun createSymbolGrid(): RecyclerView {
        symbolGrid = RecyclerView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
            clipToPadding = false
            val columns = calculateColumns()
            layoutManager = GridLayoutManager(context, columns)
        }
        return symbolGrid
    }

    private fun selectCategory(categoryId: String) {
        currentCategoryId = categoryId
        val symbols = repository.getSymbolsByCategory(categoryId)
        updateGrid(symbols)
        updateCategoryHighlight(categoryId)
    }

    private fun displaySearchResults(query: String) {
        val allSymbols = repository.getAllSymbols()
        val results = searchEngine.search(allSymbols, query)
        updateGrid(results)
    }

    private fun updateGrid(symbols: List<Symbol>) {
        symbolGrid.adapter = SymbolGridAdapter(symbols, onSymbolSelected)
    }

    private fun updateCategoryHighlight(selectedId: String) {
        for (i in 0 until categoryBar.childCount) {
            val tab = categoryBar.getChildAt(i) as? TextView ?: continue
            val isSelected = tab.tag == selectedId
            tab.setBackgroundColor(ContextCompat.getColor(context,
                if (isSelected) R.color.category_selected else R.color.category_unselected))
        }
    }

    /**
     * Called externally to type into the search field.
     */
    fun typeSearchChar(char: String) {
        searchText.append(char)
        searchField.setText(searchText.toString())
        if (searchText.length >= 2) {
            displaySearchResults(searchText.toString())
        }
    }

    fun deleteSearchChar() {
        if (searchText.isNotEmpty()) {
            searchText.deleteCharAt(searchText.length - 1)
            searchField.setText(searchText.toString())
            if (searchText.isEmpty()) {
                currentCategoryId?.let { selectCategory(it) }
            } else {
                displaySearchResults(searchText.toString())
            }
        }
    }

    private fun createBottomBar(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(44))
            setBackgroundColor(ContextCompat.getColor(context, R.color.surface))
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))

            addView(makeNavBtn("ABC") {
                (context as? com.hermetic.keyboard.ime.HermeticIME)?.switchToMainKeyboard()
            })
            addView(makeNavBtn("א") {
                (context as? com.hermetic.keyboard.ime.HermeticIME)?.switchToHebrewKeyboard()
            })
            addView(makeNavBtn("⌫") {
                (context as? com.hermetic.keyboard.ime.HermeticIME)?.deleteBackward()
            })
        }
    }

    private fun makeNavBtn(label: String, action: () -> Unit): TextView {
        return TextView(context).apply {
            text = label
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.on_background))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setBackgroundResource(R.drawable.key_background)
            setPadding(dpToPx(14), dpToPx(6), dpToPx(14), dpToPx(6))
            layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                marginEnd = dpToPx(8)
            }
            isFocusable = false
            setOnClickListener { action() }
        }
    }

    private fun calculateColumns(): Int {
        val screenWidthDp = context.resources.displayMetrics.widthPixels /
                context.resources.displayMetrics.density
        return (screenWidthDp / 56).toInt().coerceIn(5, 8)
    }

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()

    companion object {
        /** Category labels in Portuguese */
        val CATEGORY_LABELS_PT = mapOf(
            "planets" to "Planetas",
            "zodiac" to "Zodíaco",
            "elements" to "Elementos",
            "alchemy" to "Alquimia",
            "egyptian" to "Egípcio",
            "hebrew" to "Hebraico",
            "misc_esoteric" to "Esotérico"
        )
    }
}
