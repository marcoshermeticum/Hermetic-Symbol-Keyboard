package com.hermetic.keyboard.ui.panel

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hermetic.keyboard.R
import com.hermetic.keyboard.symbols.model.Symbol
import com.hermetic.keyboard.symbols.model.SymbolCategory
import com.hermetic.keyboard.symbols.repository.SymbolRepository
import com.hermetic.keyboard.symbols.search.SearchEngine

/**
 * Panel view for browsing and selecting hermetic symbols.
 * Displays a category bar at the top, a search field, and a scrollable grid of symbols.
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
        // Search bar
        addView(createSearchBar())
        // Category tabs
        addView(createCategoryBar())
        // Symbol grid
        addView(createSymbolGrid())
        // Bottom navigation bar (back to QWERTY)
        addView(createBottomBar())

        // Load first category
        val categories = repository.getCategories()
        if (categories.isNotEmpty()) {
            selectCategory(categories.first().id)
        }
    }

    private fun createSearchBar(): EditText {
        return EditText(context).apply {
            hint = context.getString(R.string.search_hint)
            setHintTextColor(ContextCompat.getColor(context, R.color.on_surface))
            setTextColor(ContextCompat.getColor(context, R.color.on_background))
            setBackgroundColor(ContextCompat.getColor(context, R.color.surface))
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            isSingleLine = true
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
            }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val query = s?.toString() ?: ""
                    if (query.isNotEmpty()) {
                        displaySearchResults(query)
                    } else {
                        currentCategoryId?.let { selectCategory(it) }
                    }
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
        return TextView(context).apply {
            // Use text labels instead of emoji-only icons
            text = category.name.split(" ").first() // Short label: "Planetary", "Zodiac", etc.
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTextColor(ContextCompat.getColor(context, R.color.on_background))
            setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8))
            setBackgroundColor(ContextCompat.getColor(context, R.color.category_unselected))
            tag = category.id
            layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginEnd = dpToPx(4)
            }

            setOnClickListener {
                selectCategory(category.id)
            }
        }
    }

    private fun createSymbolGrid(): RecyclerView {
        symbolGrid = RecyclerView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))

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
            tab.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (isSelected) R.color.category_selected else R.color.category_unselected
                )
            )
        }
    }

    private fun calculateColumns(): Int {
        val screenWidthDp = context.resources.displayMetrics.widthPixels /
                context.resources.displayMetrics.density
        return (screenWidthDp / 52).toInt().coerceIn(6, 10)
    }

    private fun createBottomBar(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(44))
            setBackgroundColor(ContextCompat.getColor(context, R.color.surface))
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))

            // ABC button - back to QWERTY
            addView(TextView(context).apply {
                text = "ABC"
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, R.color.on_background))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setBackgroundResource(R.drawable.key_background)
                setPadding(dpToPx(16), dpToPx(6), dpToPx(16), dpToPx(6))
                layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                    marginEnd = dpToPx(8)
                }
                setOnClickListener {
                    // Trigger switch back - find the IME via context
                    val ime = context as? com.hermetic.keyboard.ime.HermeticIME
                    ime?.switchToMainKeyboard()
                }
            })

            // Hebrew button
            addView(TextView(context).apply {
                text = "א"
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, R.color.on_background))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setBackgroundResource(R.drawable.key_background)
                setPadding(dpToPx(16), dpToPx(6), dpToPx(16), dpToPx(6))
                layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                    marginEnd = dpToPx(8)
                }
                setOnClickListener {
                    val ime = context as? com.hermetic.keyboard.ime.HermeticIME
                    ime?.switchToHebrewKeyboard()
                }
            })

            // Backspace
            addView(TextView(context).apply {
                text = "⌫"
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, R.color.on_background))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setBackgroundResource(R.drawable.key_background)
                setPadding(dpToPx(16), dpToPx(6), dpToPx(16), dpToPx(6))
                layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
                setOnClickListener {
                    val ime = context as? com.hermetic.keyboard.ime.HermeticIME
                    ime?.deleteBackward()
                }
            })
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
