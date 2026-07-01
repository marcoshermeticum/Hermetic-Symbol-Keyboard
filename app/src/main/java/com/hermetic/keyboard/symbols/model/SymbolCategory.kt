package com.hermetic.keyboard.symbols.model

/**
 * Represents a category of symbols in the hermetic panel.
 */
data class SymbolCategory(
    val id: String,
    val name: String,
    val icon: String,
    val description: String = "",
    val symbols: List<Symbol> = emptyList(),
    val isDynamic: Boolean = false
)
