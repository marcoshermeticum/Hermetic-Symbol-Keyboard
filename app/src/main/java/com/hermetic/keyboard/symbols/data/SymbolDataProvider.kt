package com.hermetic.keyboard.symbols.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hermetic.keyboard.R
import com.hermetic.keyboard.symbols.model.Symbol
import com.hermetic.keyboard.symbols.model.SymbolCategory

/**
 * Loads symbol data from the embedded JSON resource file.
 * This is the single source of truth for all hermetic symbol definitions.
 */
class SymbolDataProvider(private val context: Context) {

    private val gson = Gson()
    private var cachedCategories: List<SymbolCategory>? = null

    fun loadCategories(): List<SymbolCategory> {
        cachedCategories?.let { return it }

        val json = context.resources.openRawResource(R.raw.symbols)
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<SymbolsJson>() {}.type
        val data: SymbolsJson = gson.fromJson(json, type)

        val categories = data.categories.map { cat ->
            SymbolCategory(
                id = cat.id,
                name = cat.name,
                icon = cat.icon,
                description = cat.description ?: "",
                symbols = cat.symbols.mapIndexed { index, sym ->
                    Symbol(
                        id = "${cat.id}_$index",
                        categoryId = cat.id,
                        name = sym.name,
                        symbol = sym.symbol,
                        unicode = sym.unicode ?: "",
                        keywords = sym.keywords ?: emptyList(),
                        meaning = sym.meaning ?: "",
                        tradition = sym.tradition ?: emptyList(),
                        gematriaValue = sym.value
                    )
                }
            )
        }

        cachedCategories = categories
        return categories
    }

    fun getAllSymbols(): List<Symbol> {
        return loadCategories().flatMap { it.symbols }
    }

    // JSON model classes for parsing
    private data class SymbolsJson(val categories: List<CategoryJson>)
    private data class CategoryJson(
        val id: String,
        val name: String,
        val icon: String,
        val description: String?,
        val symbols: List<SymbolJson>
    )
    private data class SymbolJson(
        val name: String,
        val symbol: String,
        val unicode: String?,
        val keywords: List<String>?,
        val meaning: String?,
        val tradition: List<String>?,
        val value: Int?
    )
}
