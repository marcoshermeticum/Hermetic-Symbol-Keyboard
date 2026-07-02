package com.hermetic.keyboard.ui.panel

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
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
     * Search row with title "Símbolos Esotéricos" and clear button.
     */
    private fun createSearchRow(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(40))
            setBackgroundColor(ContextCompat.getColor(context, R.color.surface))
            setPadding(dpToPx(12), dpToPx(4), dpToPx(12), dpToPx(4))

            // Title
            addView(TextView(context).apply {
                text = "Símbolos Esotéricos"
                setTextColor(ContextCompat.getColor(context, R.color.on_background))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
                gravity = Gravity.CENTER_VERTICAL
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
