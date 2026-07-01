package com.hermetic.keyboard.symbols.search

import com.hermetic.keyboard.symbols.model.Symbol
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SearchEngineTest {

    private lateinit var searchEngine: SearchEngine
    private lateinit var testSymbols: List<Symbol>

    @Before
    fun setup() {
        searchEngine = SearchEngine()
        testSymbols = listOf(
            Symbol("planets_0", "planets", "Sun", "☉", "U+2609",
                keywords = listOf("sun", "gold", "life"), meaning = "Source of life."),
            Symbol("planets_1", "planets", "Moon", "☽", "U+263D",
                keywords = listOf("moon", "silver", "intuition"), meaning = "Intuition and dreams."),
            Symbol("planets_2", "planets", "Mercury", "☿", "U+263F",
                keywords = listOf("mercury", "communication"), meaning = "Intellect and communication."),
            Symbol("hebrew_0", "hebrew", "Aleph", "א", "U+05D0",
                keywords = listOf("aleph", "air", "spirit", "1"), meaning = "Ox, breath, spirit.", gematriaValue = 1),
            Symbol("hebrew_1", "hebrew", "Shin", "ש", "U+05E9",
                keywords = listOf("shin", "fire", "300"), meaning = "Tooth, fire, divine power.", gematriaValue = 300)
        )
    }

    @Test
    fun `search by exact name returns highest priority`() {
        val results = searchEngine.search(testSymbols, "Sun")
        assertTrue(results.isNotEmpty())
        assertEquals("Sun", results.first().name)
    }

    @Test
    fun `search is case insensitive`() {
        val results = searchEngine.search(testSymbols, "sun")
        assertTrue(results.isNotEmpty())
        assertEquals("Sun", results.first().name)
    }

    @Test
    fun `search by keyword`() {
        val results = searchEngine.search(testSymbols, "gold")
        assertTrue(results.isNotEmpty())
        assertEquals("Sun", results.first().name)
    }

    @Test
    fun `search by meaning`() {
        val results = searchEngine.search(testSymbols, "intellect")
        assertTrue(results.isNotEmpty())
        assertEquals("Mercury", results.first().name)
    }

    @Test
    fun `search Hebrew letter by name`() {
        val results = searchEngine.search(testSymbols, "aleph")
        assertTrue(results.isNotEmpty())
        assertEquals("Aleph", results.first().name)
    }

    @Test
    fun `search Hebrew letter by gematria value keyword`() {
        val results = searchEngine.search(testSymbols, "300")
        assertTrue(results.isNotEmpty())
        assertEquals("Shin", results.first().name)
    }

    @Test
    fun `empty query returns empty list`() {
        val results = searchEngine.search(testSymbols, "")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `blank query returns empty list`() {
        val results = searchEngine.search(testSymbols, "   ")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `no match returns empty list`() {
        val results = searchEngine.search(testSymbols, "xyznonexistent")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `partial name match works`() {
        val results = searchEngine.search(testSymbols, "Merc")
        assertTrue(results.isNotEmpty())
        assertEquals("Mercury", results.first().name)
    }

    @Test
    fun `search by unicode codepoint`() {
        val results = searchEngine.search(testSymbols, "U+2609")
        assertTrue(results.isNotEmpty())
        assertEquals("Sun", results.first().name)
    }
}
