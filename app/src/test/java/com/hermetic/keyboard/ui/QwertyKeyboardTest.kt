package com.hermetic.keyboard.ui

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for QwertyKeyboardView accent map and logic.
 * These test the static data and logic without needing Android context.
 */
class QwertyKeyboardTest {

    @Test
    fun `accent map contains all portuguese vowels`() {
        val map = QwertyKeyboardView.ACCENT_MAP
        assertTrue(map.containsKey("a"))
        assertTrue(map.containsKey("e"))
        assertTrue(map.containsKey("i"))
        assertTrue(map.containsKey("o"))
        assertTrue(map.containsKey("u"))
    }

    @Test
    fun `accent map contains cedilla for c`() {
        val map = QwertyKeyboardView.ACCENT_MAP
        assertTrue(map.containsKey("c"))
        assertTrue(map["c"]!!.contains("ç"))
    }

    @Test
    fun `accent map contains tilde for a and o`() {
        val map = QwertyKeyboardView.ACCENT_MAP
        assertTrue(map["a"]!!.contains("ã"))
        assertTrue(map["o"]!!.contains("õ"))
    }

    @Test
    fun `accent map contains acute accents`() {
        val map = QwertyKeyboardView.ACCENT_MAP
        assertTrue(map["a"]!!.contains("á"))
        assertTrue(map["e"]!!.contains("é"))
        assertTrue(map["i"]!!.contains("í"))
        assertTrue(map["o"]!!.contains("ó"))
        assertTrue(map["u"]!!.contains("ú"))
    }

    @Test
    fun `accent map contains circumflex`() {
        val map = QwertyKeyboardView.ACCENT_MAP
        assertTrue(map["a"]!!.contains("â"))
        assertTrue(map["e"]!!.contains("ê"))
        assertTrue(map["o"]!!.contains("ô"))
    }

    @Test
    fun `accent map for n contains ñ`() {
        val map = QwertyKeyboardView.ACCENT_MAP
        assertTrue(map.containsKey("n"))
        assertTrue(map["n"]!!.contains("ñ"))
    }

    @Test
    fun `keys without accents are not in map`() {
        val map = QwertyKeyboardView.ACCENT_MAP
        assertFalse(map.containsKey("b"))
        assertFalse(map.containsKey("d"))
        assertFalse(map.containsKey("f"))
        assertFalse(map.containsKey("g"))
        assertFalse(map.containsKey("h"))
        assertFalse(map.containsKey("j"))
        assertFalse(map.containsKey("k"))
        assertFalse(map.containsKey("l"))
        assertFalse(map.containsKey("m"))
        assertFalse(map.containsKey("p"))
        assertFalse(map.containsKey("q"))
        assertFalse(map.containsKey("r"))
        assertFalse(map.containsKey("t"))
        assertFalse(map.containsKey("v"))
        assertFalse(map.containsKey("w"))
        assertFalse(map.containsKey("x"))
        assertFalse(map.containsKey("z"))
    }

    @Test
    fun `each accent list is not empty`() {
        QwertyKeyboardView.ACCENT_MAP.forEach { (key, accents) ->
            assertTrue("Accents for '$key' should not be empty", accents.isNotEmpty())
        }
    }

    @Test
    fun `no duplicate accents in any list`() {
        QwertyKeyboardView.ACCENT_MAP.forEach { (key, accents) ->
            assertEquals("Duplicates found for '$key'", accents.size, accents.toSet().size)
        }
    }
}
