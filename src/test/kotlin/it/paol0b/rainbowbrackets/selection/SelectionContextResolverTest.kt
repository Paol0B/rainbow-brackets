package it.paol0b.rainbowbrackets.selection

import it.paol0b.rainbowbrackets.core.BracketToken
import it.paol0b.rainbowbrackets.core.BracketType
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [SelectionContextResolver].
 * IDE-independent: tests pure logic using pre-built BracketToken lists.
 */
class SelectionContextResolverTest {

    // --- findEnclosingPairs ---

    @Test
    fun `empty tokens returns no pairs`() {
        val result = SelectionContextResolver.findEnclosingPairs(emptyList(), 5, 10)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `selection outside all brackets returns no pairs`() {
        val tokens = listOf(
            BracketToken(BracketType.ROUND, 10, true, 0, matchOffset = 20),
            BracketToken(BracketType.ROUND, 20, false, 0, matchOffset = 10)
        )
        val result = SelectionContextResolver.findEnclosingPairs(tokens, 0, 5)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `selection inside brackets returns enclosing pair`() {
        val tokens = listOf(
            BracketToken(BracketType.ROUND, 0, true, 0, matchOffset = 20),
            BracketToken(BracketType.ROUND, 20, false, 0, matchOffset = 0)
        )
        val result = SelectionContextResolver.findEnclosingPairs(tokens, 5, 15)
        assertEquals(1, result.size)
        assertEquals(0, result[0].openOffset)
        assertEquals(20, result[0].closeOffset)
    }

    @Test
    fun `nested brackets return multiple enclosing pairs`() {
        // ( [ ... ] )
        val tokens = listOf(
            BracketToken(BracketType.ROUND, 0, true, 0, matchOffset = 30),
            BracketToken(BracketType.SQUARE, 5, true, 0, matchOffset = 25),
            BracketToken(BracketType.SQUARE, 25, false, 0, matchOffset = 5),
            BracketToken(BracketType.ROUND, 30, false, 0, matchOffset = 0)
        )
        val result = SelectionContextResolver.findEnclosingPairs(tokens, 10, 20)
        assertEquals(2, result.size) // Both ( and [ enclose selection
    }

    @Test
    fun `selection equal to bracket positions returns pair`() {
        val tokens = listOf(
            BracketToken(BracketType.ROUND, 5, true, 0, matchOffset = 15),
            BracketToken(BracketType.ROUND, 15, false, 0, matchOffset = 5)
        )
        val result = SelectionContextResolver.findEnclosingPairs(tokens, 5, 15)
        assertEquals(1, result.size)
    }

    @Test
    fun `unmatched bracket is ignored`() {
        val tokens = listOf(
            BracketToken(BracketType.ROUND, 0, true, 0, matchOffset = -1) // unmatched
        )
        val result = SelectionContextResolver.findEnclosingPairs(tokens, 0, 10)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `zero-length selection returns no pairs`() {
        val tokens = listOf(
            BracketToken(BracketType.ROUND, 0, true, 0, matchOffset = 20),
            BracketToken(BracketType.ROUND, 20, false, 0, matchOffset = 0)
        )
        val result = SelectionContextResolver.findEnclosingPairs(tokens, 10, 10)
        assertTrue(result.isEmpty())
    }

    // --- findInnermostEnclosingPair ---

    @Test
    fun `innermost pair found at caret`() {
        // ( { | } )
        val tokens = listOf(
            BracketToken(BracketType.ROUND, 0, true, 0, matchOffset = 20),
            BracketToken(BracketType.CURLY, 5, true, 0, matchOffset = 15),
            BracketToken(BracketType.CURLY, 15, false, 0, matchOffset = 5),
            BracketToken(BracketType.ROUND, 20, false, 0, matchOffset = 0)
        )
        val result = SelectionContextResolver.findInnermostEnclosingPair(tokens, 10)
        assertNotNull(result)
        assertEquals(5, result!!.openOffset)
        assertEquals(15, result.closeOffset)
    }

    @Test
    fun `caret outside all brackets returns null`() {
        val tokens = listOf(
            BracketToken(BracketType.ROUND, 10, true, 0, matchOffset = 20),
            BracketToken(BracketType.ROUND, 20, false, 0, matchOffset = 10)
        )
        val result = SelectionContextResolver.findInnermostEnclosingPair(tokens, 5)
        assertNull(result)
    }

    @Test
    fun `caret exactly on bracket returns enclosing pair if any above`() {
        val tokens = listOf(
            BracketToken(BracketType.ROUND, 0, true, 0, matchOffset = 20),
            BracketToken(BracketType.CURLY, 5, true, 0, matchOffset = 15),
            BracketToken(BracketType.CURLY, 15, false, 0, matchOffset = 5),
            BracketToken(BracketType.ROUND, 20, false, 0, matchOffset = 0)
        )
        // Caret at the opening { position
        val result = SelectionContextResolver.findInnermostEnclosingPair(tokens, 5)
        // { at 5 does NOT enclose 5 (requires strictly inside), so ( at 0 is the enclosing pair
        assertNotNull(result)
        assertEquals(0, result!!.openOffset)
    }

    // --- AffectedPair properties ---

    @Test
    fun `affected pair inner range is correct`() {
        val tokens = listOf(
            BracketToken(BracketType.ROUND, 10, true, 2, matchOffset = 50),
            BracketToken(BracketType.ROUND, 50, false, 2, matchOffset = 10)
        )
        val result = SelectionContextResolver.findEnclosingPairs(tokens, 20, 40)
        assertEquals(1, result.size)
        assertEquals(11, result[0].innerStart) // openOffset + 1
        assertEquals(50, result[0].innerEnd)   // closeOffset
        assertEquals(2, result[0].level)
    }
}
