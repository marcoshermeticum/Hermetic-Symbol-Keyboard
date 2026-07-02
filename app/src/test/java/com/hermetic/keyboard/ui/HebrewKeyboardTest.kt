package com.hermetic.keyboard.ui

import com.hermetic.keyboard.ui.hebrew.HebrewKeyboardView
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for the Hebrew transliterated keyboard layout and data.
 */
class HebrewKeyboardTest {

    @Test
    fun `hebrew layout has 4 rows`() {
        val rows = HebrewKeyboardView.getHebrewKeyRows()
        assertEquals(4, rows.size)
    }

    @Test
    fun `all 22 base hebrew letters are present`() {
        val rows = HebrewKeyboardView.getHebrewKeyRows()
        val allKeys = rows.flatten()
        val hebrewChars = allKeys.filter { !it.isSpecialKey && !it.isSofit }.map { it.output }

        // 22 base letters
        val expectedLetters = listOf("א", "ב", "ג", "ד", "ה", "ו", "ז", "ח", "ט", "י",
            "כ", "ל", "מ", "נ", "ס", "ע", "פ", "צ", "ק", "ר", "ש", "ת")

        expectedLetters.forEach { letter ->
            assertTrue("Missing letter: $letter", hebrewChars.contains(letter))
        }
    }

    @Test
    fun `all 5 sofit forms are present`() {
        val rows = HebrewKeyboardView.getHebrewKeyRows()
        val allKeys = rows.flatten()
        val sofitKeys = allKeys.filter { it.isSofit }

        assertEquals(5, sofitKeys.size)

        val sofitChars = sofitKeys.map { it.output }
        assertTrue(sofitChars.contains("ך")) // Kaf Sofit
        assertTrue(sofitChars.contains("ם")) // Mem Sofit
        assertTrue(sofitChars.contains("ן")) // Nun Sofit
        assertTrue(sofitChars.contains("ף")) // Pe Sofit
        assertTrue(sofitChars.contains("ץ")) // Tsade Sofit
    }

    @Test
    fun `special keys are in row 4`() {
        val rows = HebrewKeyboardView.getHebrewKeyRows()
        val row4 = rows[3]
        val specialKeys = row4.filter { it.isSpecialKey }

        assertTrue(specialKeys.isNotEmpty())
        assertTrue(specialKeys.any { it.output == "BACKSPACE" })
        assertTrue(specialKeys.any { it.output == "SWITCH_TO_ALPHA" })
        assertTrue(specialKeys.any { it.output == " " })
    }

    @Test
    fun `each key has a non-empty label`() {
        val rows = HebrewKeyboardView.getHebrewKeyRows()
        rows.flatten().forEach { key ->
            assertTrue("Key has empty label", key.label.isNotEmpty())
        }
    }

    @Test
    fun `each non-special key has a valid hebrew output`() {
        val rows = HebrewKeyboardView.getHebrewKeyRows()
        val hebrewKeys = rows.flatten().filter { !it.isSpecialKey }

        hebrewKeys.forEach { key ->
            assertTrue("Key ${key.label} output should be single char",
                key.output.length == 1)
            // Hebrew Unicode range: U+05D0 to U+05EA
            val codePoint = key.output[0].code
            assertTrue("Key ${key.label} output '${key.output}' not in Hebrew range",
                codePoint in 0x05D0..0x05EA)
        }
    }

    @Test
    fun `gematria values are correctly assigned`() {
        val rows = HebrewKeyboardView.getHebrewKeyRows()
        val allKeys = rows.flatten().filter { !it.isSpecialKey }

        val aleph = allKeys.find { it.output == "א" }
        assertNotNull(aleph)
        assertEquals(1, aleph!!.gematriaValue)

        val shin = allKeys.find { it.output == "ש" }
        assertNotNull(shin)
        assertEquals(300, shin!!.gematriaValue)

        val tav = allKeys.find { it.output == "ת" }
        assertNotNull(tav)
        assertEquals(400, tav!!.gematriaValue)
    }

    @Test
    fun `sofit values are 500-900`() {
        val rows = HebrewKeyboardView.getHebrewKeyRows()
        val sofitKeys = rows.flatten().filter { it.isSofit }

        sofitKeys.forEach { key ->
            assertTrue("Sofit ${key.label} should have value >= 500",
                key.gematriaValue >= 500)
            assertTrue("Sofit ${key.label} should have value <= 900",
                key.gematriaValue <= 900)
        }
    }

    @Test
    fun `no duplicate outputs in layout`() {
        val rows = HebrewKeyboardView.getHebrewKeyRows()
        val outputs = rows.flatten().filter { !it.isSpecialKey }.map { it.output }
        assertEquals("Duplicate outputs found", outputs.size, outputs.toSet().size)
    }

    @Test
    fun `label is transliterated - not hebrew`() {
        val rows = HebrewKeyboardView.getHebrewKeyRows()
        val hebrewKeys = rows.flatten().filter { !it.isSpecialKey }

        hebrewKeys.forEach { key ->
            // Label should be Latin characters (transliterated)
            val isLatin = key.label.all { c -> c in 'A'..'Z' || c in 'a'..'z' || c == '·' || c == '*' || c == ' ' }
            assertTrue("Key label '${key.label}' should be transliterated Latin", isLatin)
        }
    }
}
