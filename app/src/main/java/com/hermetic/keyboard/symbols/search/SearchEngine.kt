package com.hermetic.keyboard.symbols.search

import com.hermetic.keyboard.symbols.model.Symbol

/**
 * Search engine for finding symbols by name, keywords, or meaning.
 * Uses case-insensitive matching with support for partial queries.
 */
class SearchEngine {

    /**
     * Search symbols matching the given query.
     * Searches across name, keywords, and meaning fields.
     * Results are ranked by relevance:
     *  1. Exact name match
     *  2. Name starts with query
     *  3. Keyword match
     *  4. Meaning contains query
     */
    fun search(symbols: List<Symbol>, query: String): List<Symbol> {
        if (query.isBlank()) return emptyList()

        val normalizedQuery = query.trim().lowercase()

        data class ScoredSymbol(val symbol: Symbol, val score: Int)

        return symbols.mapNotNull { symbol ->
            val score = calculateScore(symbol, normalizedQuery)
            if (score > 0) ScoredSymbol(symbol, score) else null
        }
            .sortedByDescending { it.score }
            .map { it.symbol }
    }

    private fun calculateScore(symbol: Symbol, query: String): Int {
        var score = 0

        val nameLower = symbol.name.lowercase()

        // Exact name match (highest priority)
        if (nameLower == query) {
            score += 100
        }
        // Name starts with query
        else if (nameLower.startsWith(query)) {
            score += 80
        }
        // Name contains query
        else if (nameLower.contains(query)) {
            score += 60
        }

        // Keyword match
        if (symbol.keywords.any { it.lowercase() == query }) {
            score += 70
        } else if (symbol.keywords.any { it.lowercase().contains(query) }) {
            score += 40
        }

        // Meaning contains query
        if (symbol.meaning.lowercase().contains(query)) {
            score += 20
        }

        // Unicode codepoint match
        if (symbol.unicode.lowercase().contains(query)) {
            score += 30
        }

        return score
    }
}
