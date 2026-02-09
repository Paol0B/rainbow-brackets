package it.paol0b.rainbowbrackets.core

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [BracketToken].
 */
class BracketTokenTest {

    @Test
    fun `colorIndex cycles modulo MAX_LEVELS`() {
        for (level in 0..20) {
            val token = BracketToken(BracketType.ROUND, 0, true, level)
            assertEquals(level % BracketToken.MAX_LEVELS, token.colorIndex)
        }
    }

    @Test
    fun `isMismatched returns true when matchOffset is negative`() {
        val token = BracketToken(BracketType.ROUND, 0, true, 0, matchOffset = -1)
        assertTrue(token.isMismatched)
    }

    @Test
    fun `isMismatched returns false when matchOffset is valid`() {
        val token = BracketToken(BracketType.ROUND, 0, true, 0, matchOffset = 10)
        assertFalse(token.isMismatched)
    }

    @Test
    fun `isInRange returns true when offset is within range`() {
        val token = BracketToken(BracketType.ROUND, 5, true, 0)
        assertTrue(token.isInRange(0, 10))
        assertTrue(token.isInRange(5, 10))
    }

    @Test
    fun `isInRange returns false when offset is outside range`() {
        val token = BracketToken(BracketType.ROUND, 5, true, 0)
        assertFalse(token.isInRange(6, 10))
        assertFalse(token.isInRange(0, 5))
    }

    @Test
    fun `MAX_LEVELS is 6`() {
        assertEquals(6, BracketToken.MAX_LEVELS)
    }

    @Test
    fun `data class equality works`() {
        val a = BracketToken(BracketType.ROUND, 0, true, 0, 10)
        val b = BracketToken(BracketType.ROUND, 0, true, 0, 10)
        assertEquals(a, b)
    }

    @Test
    fun `data class copy works`() {
        val original = BracketToken(BracketType.ROUND, 0, true, 0, -1)
        val updated = original.copy(matchOffset = 10)
        assertEquals(10, updated.matchOffset)
        assertEquals(-1, original.matchOffset)
    }
}
