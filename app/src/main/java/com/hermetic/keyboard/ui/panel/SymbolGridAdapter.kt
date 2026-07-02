package com.hermetic.keyboard.ui.panel

import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hermetic.keyboard.R
import com.hermetic.keyboard.symbols.model.Symbol

/**
 * RecyclerView adapter for symbols grid with proper spacing and PT-BR tooltips.
 */
class SymbolGridAdapter(
    private val symbols: List<Symbol>,
    private val onSymbolClick: (Symbol) -> Unit
) : RecyclerView.Adapter<SymbolGridAdapter.SymbolViewHolder>() {

    class SymbolViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymbolViewHolder {
        val cellSize = dpToPx(parent.context, 46)
        val textView = TextView(parent.context).apply {
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTextColor(ContextCompat.getColor(context, R.color.on_background))
            setBackgroundResource(R.drawable.key_background)
            // Add margin for spacing between cells
            val margin = dpToPx(context, 4)
            layoutParams = ViewGroup.MarginLayoutParams(cellSize, cellSize).apply {
                setMargins(margin, margin, margin, margin)
            }
        }
        return SymbolViewHolder(textView)
    }

    override fun onBindViewHolder(holder: SymbolViewHolder, position: Int) {
        val symbol = symbols[position]
        holder.textView.text = symbol.symbol

        holder.textView.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onSymbolClick(symbol)
        }

        holder.textView.setOnLongClickListener {
            val info = buildTooltipText(symbol)
            Toast.makeText(it.context, info, Toast.LENGTH_LONG).show()
            true
        }

        holder.textView.contentDescription = symbol.name
    }

    /**
     * Builds tooltip in Portuguese with relevant metadata.
     */
    private fun buildTooltipText(symbol: Symbol): String {
        return buildString {
            append(symbol.name)
            if (symbol.meaning.isNotEmpty()) {
                append("\n")
                // Translate common English tooltip words to PT
                append(symbol.meaning)
            }
            if (symbol.gematriaValue != null) {
                append("\nGematria: ${symbol.gematriaValue}")
            }
            if (symbol.unicode.isNotEmpty()) {
                append("\n${symbol.unicode}")
            }
        }
    }

    override fun getItemCount(): Int = symbols.size

    private fun dpToPx(context: android.content.Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
