package com.hermetic.keyboard.symbols.model

/**
 * Represents a key in the Hebrew transliterated keyboard layout.
 * The label is shown on the key (transliterated name), and the output
 * is the Hebrew character that gets inserted into the text field.
 */
data class HebrewKey(
    val label: String,
    val output: String,
    val hebrewName: String = label,
    val gematriaValue: Int = 0,
    val isSpecialKey: Boolean = false,
    val isSofit: Boolean = false
)
