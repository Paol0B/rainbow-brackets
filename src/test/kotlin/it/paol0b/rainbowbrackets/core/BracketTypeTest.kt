package it.paol0b.rainbowbrackets.core

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [BracketType].
 */
class BracketTypeTest {

    @Test
    fun `fromOpenChar returns correct type`() {
        assertEquals(BracketType.ROUND, BracketType.fromOpenChar('('))
        assertEquals(BracketType.SQUARE, BracketType.fromOpenChar('['))
        assertEquals(BracketType.CURLY, BracketType.fromOpenChar('{'))
        assertEquals(BracketType.ANGLE, BracketType.fromOpenChar('<'))
    }

    @Test
    fun `fromCloseChar returns correct type`() {
        assertEquals(BracketType.ROUND, BracketType.fromCloseChar(')'))
        assertEquals(BracketType.SQUARE, BracketType.fromCloseChar(']'))
        assertEquals(BracketType.CURLY, BracketType.fromCloseChar('}'))
        assertEquals(BracketType.ANGLE, BracketType.fromCloseChar('>'))
    }

    @Test
    fun `fromChar returns type for either open or close`() {
        assertEquals(BracketType.ROUND, BracketType.fromChar('('))
        assertEquals(BracketType.ROUND, BracketType.fromChar(')'))
        assertEquals(BracketType.CURLY, BracketType.fromChar('{'))
        assertEquals(BracketType.CURLY, BracketType.fromChar('}'))
    }

    @Test
    fun `fromOpenChar returns null for non-bracket`() {
        assertNull(BracketType.fromOpenChar('a'))
        assertNull(BracketType.fromOpenChar('+'))
    }

    @Test
    fun `isBracketChar identifies all brackets`() {
        val brackets = charArrayOf('(', ')', '[', ']', '{', '}', '<', '>')
        for (ch in brackets) {
            assertTrue("$ch should be a bracket", BracketType.isBracketChar(ch))
        }
    }

    @Test
    fun `isBracketChar rejects non-brackets`() {
        val nonBrackets = charArrayOf('a', '1', '+', ' ', '\n', '=')
        for (ch in nonBrackets) {
            assertFalse("$ch should not be a bracket", BracketType.isBracketChar(ch))
        }
    }

    @Test
    fun `isOpenChar only matches openers`() {
        assertTrue(BracketType.isOpenChar('('))
        assertTrue(BracketType.isOpenChar('{'))
        assertFalse(BracketType.isOpenChar(')'))
        assertFalse(BracketType.isOpenChar('}'))
    }

    @Test
    fun `isCloseChar only matches closers`() {
        assertTrue(BracketType.isCloseChar(')'))
        assertTrue(BracketType.isCloseChar('}'))
        assertFalse(BracketType.isCloseChar('('))
        assertFalse(BracketType.isCloseChar('{'))
    }

    @Test
    fun `bracket type properties are correct`() {
        assertEquals('(', BracketType.ROUND.open)
        assertEquals(')', BracketType.ROUND.close)
        assertEquals('[', BracketType.SQUARE.open)
        assertEquals(']', BracketType.SQUARE.close)
        assertEquals('{', BracketType.CURLY.open)
        assertEquals('}', BracketType.CURLY.close)
        assertEquals('<', BracketType.ANGLE.open)
        assertEquals('>', BracketType.ANGLE.close)
    }
}
