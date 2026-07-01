package com.hermetic.keyboard.ui.panel

import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hermetic.keyboard.R
import com.hermetic.keyboard.symbols.model.Symbol

/**
 * RecyclerView adapter for displaying symbols in a grid layout.
 */
class SymbolGridAdapter(
    private val symbols: List<Symbol>,
    private val onSymbolClick: (Symbol) -> Unit
) : RecyclerView.Adapter<SymbolGridAdapter.SymbolViewHolder>() {

    class SymbolViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymbolViewHolder {
        val cellSize = dpToPx(parent.context, 48)
        val textView = TextView(parent.context).apply {
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTextColor(ContextCompat.getColor(context, R.color.on_background))
            setBackgroundResource(R.drawable.key_background)
            layoutParams = ViewGroup.LayoutParams(cellSize, cellSize)
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
            val info = buildString {
                append(symbol.name)
                if (symbol.meaning.isNotEmpty()) append("\n${symbol.meaning}")
                if (symbol.gematriaValue != null) append("\nGematria: ${symbol.gematriaValue}")
            }
            Toast.makeText(it.context, info, Toast.LENGTH_LONG).show()
            true
        }

        holder.textView.contentDescription = symbol.name
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
