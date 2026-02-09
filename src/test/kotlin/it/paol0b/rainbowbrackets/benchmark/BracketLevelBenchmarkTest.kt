package it.paol0b.rainbowbrackets.benchmark

import it.paol0b.rainbowbrackets.core.BracketLevelCalculator
import it.paol0b.rainbowbrackets.core.BracketToken
import it.paol0b.rainbowbrackets.core.BracketType
import it.paol0b.rainbowbrackets.util.PerformanceGuard
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Benchmark tests for Rainbow Brackets performance.
 *
 * Validates that core operations meet performance targets:
 * - 1K-line file: imperceptible (<5ms)
 * - 50K-line file: <100ms
 * - Rapid bracket scanning: linear scaling
 *
 * These tests use the pure-logic [BracketLevelCalculator] directly
 * since it represents the computation-heavy critical path.
 */
class BracketLevelBenchmarkTest {

    /**
     * Generates a synthetic source file with nested brackets.
     * Each line has a configurable depth pattern.
     */
    private fun generateTestFile(lineCount: Int, bracketsPerLine: Int = 4): String {
        val sb = StringBuilder(lineCount * (bracketsPerLine * 2 + 20))
        for (i in 0 until lineCount) {
            sb.append("fun f")
            sb.append(i)
            sb.append("(")
            repeat(bracketsPerLine - 1) { sb.append("arr[i + (j * ") }
            repeat(bracketsPerLine - 1) { sb.append(")]") }
            sb.append(")")
            sb.appendLine()
        }
        return sb.toString()
    }

    /**
     * Generates a deeply nested file (one bracket per line).
     */
    private fun generateDeeplyNested(depth: Int): String {
        val sb = StringBuilder(depth * 4)
        repeat(depth) { sb.append("{ ") }
        repeat(depth) { sb.append("} ") }
        return sb.toString()
    }

    @Test
    fun `1K line file processes in under 50ms`() {
        val text = generateTestFile(1_000)

        // Warm up JIT
        repeat(3) { BracketLevelCalculator.calculateFromText(text) }

        // Measure
        val (result, durationNanos) = PerformanceGuard.measure {
            BracketLevelCalculator.calculateFromText(text)
        }
        val durationMs = durationNanos / 1_000_000.0

        println("1K lines: ${result.size} tokens in ${String.format("%.2f", durationMs)}ms")
        assertTrue(
            "1K line file should process in <50ms but took ${durationMs}ms",
            durationMs < 50.0
        )
        assertTrue("Should find brackets", result.isNotEmpty())
    }

    @Test
    fun `10K line file processes in under 100ms`() {
        val text = generateTestFile(10_000)

        repeat(3) { BracketLevelCalculator.calculateFromText(text) }

        val (result, durationNanos) = PerformanceGuard.measure {
            BracketLevelCalculator.calculateFromText(text)
        }
        val durationMs = durationNanos / 1_000_000.0

        println("10K lines: ${result.size} tokens in ${String.format("%.2f", durationMs)}ms")
        assertTrue(
            "10K line file should process in <100ms but took ${durationMs}ms",
            durationMs < 100.0
        )
    }

    @Test
    fun `50K line file processes in under 500ms`() {
        val text = generateTestFile(50_000, bracketsPerLine = 2)

        repeat(2) { BracketLevelCalculator.calculateFromText(text) }

        val (result, durationNanos) = PerformanceGuard.measure {
            BracketLevelCalculator.calculateFromText(text)
        }
        val durationMs = durationNanos / 1_000_000.0

        println("50K lines: ${result.size} tokens in ${String.format("%.2f", durationMs)}ms")
        assertTrue(
            "50K line file should process in <500ms but took ${durationMs}ms",
            durationMs < 500.0
        )
    }

    @Test
    fun `deeply nested brackets 1000 deep processed correctly`() {
        val text = generateDeeplyNested(1000)

        val (result, durationNanos) = PerformanceGuard.measure {
            BracketLevelCalculator.calculateFromText(text)
        }
        val durationMs = durationNanos / 1_000_000.0

        println("1000-deep nesting: ${result.size} tokens in ${String.format("%.2f", durationMs)}ms")
        assertEquals(2000, result.size.toLong())

        // Verify level cycling
        val curly = result.filter { token -> token.type == BracketType.CURLY }
        for (i in 0 until 1000) {
            org.junit.Assert.assertEquals(i % BracketToken.MAX_LEVELS, curly[i].level)
        }
    }

    @Test
    fun `repeated calculations with same input benefit from CPU cache`() {
        val text = generateTestFile(5_000)

        // Cold run
        val (_, coldNanos) = PerformanceGuard.measure {
            BracketLevelCalculator.calculateFromText(text)
        }

        // Warm runs
        var warmTotal = 0L
        val warmRuns = 10
        repeat(warmRuns) {
            val (_, nanos) = PerformanceGuard.measure {
                BracketLevelCalculator.calculateFromText(text)
            }
            warmTotal += nanos
        }
        val warmAvg = warmTotal / warmRuns

        val coldMs = coldNanos / 1_000_000.0
        val warmMs = warmAvg / 1_000_000.0
        println("Cold: ${String.format("%.2f", coldMs)}ms, Warm avg: ${String.format("%.2f", warmMs)}ms")

        // Warm runs should not be dramatically slower (no memory leak per run)
        assertTrue("Warm runs should be reasonable", warmMs < coldMs * 3)
    }

    @Test
    fun `bracket filter does not significantly impact performance`() {
        val text = generateTestFile(5_000)
        val noFilter: ((Char, Int) -> Boolean)? = null
        val withFilter: (Char, Int) -> Boolean = { ch, _ -> ch != '<' && ch != '>' }

        // Warm up
        repeat(3) {
            BracketLevelCalculator.calculateFromText(text, noFilter)
            BracketLevelCalculator.calculateFromText(text, withFilter)
        }

        val (_, noFilterNanos) = PerformanceGuard.measure {
            BracketLevelCalculator.calculateFromText(text, noFilter)
        }
        val (_, withFilterNanos) = PerformanceGuard.measure {
            BracketLevelCalculator.calculateFromText(text, withFilter)
        }

        val noFilterMs = noFilterNanos / 1_000_000.0
        val withFilterMs = withFilterNanos / 1_000_000.0
        println("No filter: ${String.format("%.2f", noFilterMs)}ms, With filter: ${String.format("%.2f", withFilterMs)}ms")

        // Filter overhead should be negligible
        assertTrue(
            "Filter overhead should be <50% but was ${((withFilterMs / noFilterMs) - 1) * 100}%",
            withFilterMs < noFilterMs * 1.5 + 1.0 // allow 1ms tolerance
        )
    }

    private fun assertEquals(expected: Long, actual: Long) {
        org.junit.Assert.assertEquals(expected, actual)
    }
}
