package it.paol0b.rainbowbrackets.core

import it.paol0b.rainbowbrackets.core.BracketLevelCalculator.BracketChar
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [BracketLevelCalculator].
 * IDE-independent: tests pure computation logic.
 */
class BracketLevelCalculatorTest {

    // --- Basic nesting ---

    @Test
    fun `empty input produces no tokens`() {
        val result = BracketLevelCalculator.calculateLevels(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `single bracket pair has level 0`() {
        val brackets = listOf(
            BracketChar('(', 0),
            BracketChar(')', 5)
        )
        val result = BracketLevelCalculator.calculateLevels(brackets)
        assertEquals(2, result.size)
        assertEquals(0, result[0].level) // (
        assertEquals(0, result[1].level) // )
        assertEquals(5, result[0].matchOffset)
        assertEquals(0, result[1].matchOffset)
    }

    @Test
    fun `nested brackets increment levels`() {
        // ((()))
        val brackets = listOf(
            BracketChar('(', 0),
            BracketChar('(', 1),
            BracketChar('(', 2),
            BracketChar(')', 3),
            BracketChar(')', 4),
            BracketChar(')', 5)
        )
        val result = BracketLevelCalculator.calculateLevels(brackets)
        assertEquals(6, result.size)
        assertEquals(0, result[0].level) // outer (
        assertEquals(1, result[1].level) // middle (
        assertEquals(2, result[2].level) // inner (
        assertEquals(2, result[3].level) // inner )
        assertEquals(1, result[4].level) // middle )
        assertEquals(0, result[5].level) // outer )
    }

    @Test
    fun `sequential bracket pairs all have level 0`() {
        // ()()()
        val brackets = listOf(
            BracketChar('(', 0), BracketChar(')', 1),
            BracketChar('(', 2), BracketChar(')', 3),
            BracketChar('(', 4), BracketChar(')', 5)
        )
        val result = BracketLevelCalculator.calculateLevels(brackets)
        assertEquals(6, result.size)
        for (token in result) {
            assertEquals(0, token.level)
        }
    }

    // --- Level cycling (overflow) ---

    @Test
    fun `level wraps at MAX_LEVELS boundary`() {
        // 7 nested brackets → level 6 wraps to 0
        val brackets = mutableListOf<BracketChar>()
        for (i in 0..6) {
            brackets.add(BracketChar('(', i))
        }
        for (i in 7..13) {
            brackets.add(BracketChar(')', i))
        }
        val result = BracketLevelCalculator.calculateLevels(brackets)
        assertEquals(14, result.size)

        // Opening brackets: levels 0,1,2,3,4,5,0
        assertEquals(0, result[0].level)
        assertEquals(1, result[1].level)
        assertEquals(2, result[2].level)
        assertEquals(3, result[3].level)
        assertEquals(4, result[4].level)
        assertEquals(5, result[5].level)
        assertEquals(0, result[6].level) // wraps back to 0
    }

    @Test
    fun `deep nesting cycles correctly for 12 levels`() {
        val brackets = mutableListOf<BracketChar>()
        for (i in 0..11) {
            brackets.add(BracketChar('(', i))
        }
        for (i in 12..23) {
            brackets.add(BracketChar(')', i))
        }
        val result = BracketLevelCalculator.calculateLevels(brackets)
        assertEquals(24, result.size)

        // First 6 openers: 0,1,2,3,4,5
        // Next 6 openers: 0,1,2,3,4,5 (second cycle)
        for (i in 0..11) {
            assertEquals(i % BracketToken.MAX_LEVELS, result[i].level)
        }
    }

    // --- Mixed bracket types ---

    @Test
    fun `different bracket types have independent levels`() {
        // ({[]})
        val brackets = listOf(
            BracketChar('(', 0),
            BracketChar('{', 1),
            BracketChar('[', 2),
            BracketChar(']', 3),
            BracketChar('}', 4),
            BracketChar(')', 5)
        )
        val result = BracketLevelCalculator.calculateLevels(brackets)
        assertEquals(6, result.size)
        // Each type is at its own level 0 (independent stacks)
        assertEquals(0, result[0].level) // (
        assertEquals(0, result[1].level) // {
        assertEquals(0, result[2].level) // [
        assertEquals(0, result[3].level) // ]
        assertEquals(0, result[4].level) // }
        assertEquals(0, result[5].level) // )
    }

    @Test
    fun `nested same-type within mixed brackets`() {
        // (({()}))
        val brackets = listOf(
            BracketChar('(', 0),
            BracketChar('(', 1),
            BracketChar('{', 2),
            BracketChar('(', 3),
            BracketChar(')', 4),
            BracketChar('}', 5),
            BracketChar(')', 6),
            BracketChar(')', 7)
        )
        val result = BracketLevelCalculator.calculateLevels(brackets)
        assertEquals(8, result.size)
        assertEquals(0, result[0].level) // outer (
        assertEquals(1, result[1].level) // middle (
        assertEquals(0, result[2].level) // { (independent type)
        assertEquals(2, result[3].level) // inner (
        assertEquals(2, result[4].level) // inner )
        assertEquals(0, result[5].level) // }
        assertEquals(1, result[6].level) // middle )
        assertEquals(0, result[7].level) // outer )
    }

    // --- Matching ---

    @Test
    fun `matched brackets have correct match offsets`() {
        val brackets = listOf(
            BracketChar('(', 10),
            BracketChar(')', 20)
        )
        val result = BracketLevelCalculator.calculateLevels(brackets)
        assertEquals(20, result[0].matchOffset) // opener points to closer
        assertEquals(10, result[1].matchOffset) // closer points to opener
    }

    @Test
    fun `unmatched opener has matchOffset of -1`() {
        val brackets = listOf(
            BracketChar('(', 0)
        )
        val result = BracketLevelCalculator.calculateLevels(brackets)
        assertEquals(1, result.size)
        assertTrue(result[0].isMismatched)
        assertEquals(-1, result[0].matchOffset)
    }

    @Test
    fun `unmatched closer has matchOffset of -1`() {
        val brackets = listOf(
            BracketChar(')', 0)
        )
        val result = BracketLevelCalculator.calculateLevels(brackets)
        assertEquals(1, result.size)
        assertTrue(result[0].isMismatched)
        assertEquals(-1, result[0].matchOffset)
    }

    // --- calculateFromText ---

    @Test
    fun `calculateFromText with simple code`() {
        val text = "func(a, b)"
        val result = BracketLevelCalculator.calculateFromText(text)
        // Should find ( at 4 and ) at 9
        val bracketTokens = result.filter {
            it.type == BracketType.ROUND
        }
        assertEquals(2, bracketTokens.size)
        assertEquals(4, bracketTokens[0].offset)
        assertEquals(9, bracketTokens[1].offset)
        assertEquals(0, bracketTokens[0].level)
        assertEquals(0, bracketTokens[1].level)
    }

    @Test
    fun `calculateFromText with bracket filter excludes angles`() {
        val text = "a < b && c > d"
        val filter: (Char, Int) -> Boolean = { char, _ -> char != '<' && char != '>' }
        val result = BracketLevelCalculator.calculateFromText(text, filter)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `calculateFromText handles nested curly braces`() {
        val text = "{ { { } } }"
        val result = BracketLevelCalculator.calculateFromText(text)
        val curly = result.filter { it.type == BracketType.CURLY }
        assertEquals(6, curly.size)
        assertEquals(0, curly[0].level)
        assertEquals(1, curly[1].level)
        assertEquals(2, curly[2].level)
        assertEquals(2, curly[3].level)
        assertEquals(1, curly[4].level)
        assertEquals(0, curly[5].level)
    }

    // --- Edge cases ---

    @Test
    fun `angle brackets counted as brackets by default`() {
        val text = "List<Map<String, Int>>"
        val result = BracketLevelCalculator.calculateFromText(text)
        val angles = result.filter { it.type == BracketType.ANGLE }
        assertEquals(4, angles.size)
    }

    @Test
    fun `interleaved mismatched brackets still processes correctly`() {
        // ( [ ) ] — each type has independent stack, so ( matches ), [ matches ]
        val brackets = listOf(
            BracketChar('(', 0),
            BracketChar('[', 1),
            BracketChar(')', 2),
            BracketChar(']', 3)
        )
        val result = BracketLevelCalculator.calculateLevels(brackets)
        assertEquals(4, result.size)
        // ( at 0 matches ) at 2
        assertEquals(2, result[0].matchOffset)
        // [ at 1 matches ] at 3
        assertEquals(3, result[1].matchOffset)
    }
}
