package com.hermetic.keyboard.symbols.model

/**
 * Represents a single symbol in the hermetic keyboard.
 */
data class Symbol(
    val id: String,
    val categoryId: String,
    val name: String,
    val symbol: String,
    val unicode: String,
    val keywords: List<String> = emptyList(),
    val meaning: String = "",
    val tradition: List<String> = emptyList(),
    val gematriaValue: Int? = null
)
