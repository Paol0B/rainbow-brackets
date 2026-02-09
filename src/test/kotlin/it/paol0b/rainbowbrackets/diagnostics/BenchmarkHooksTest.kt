package it.paol0b.rainbowbrackets.diagnostics

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [BenchmarkHooks] metrics collection.
 */
class BenchmarkHooksTest {

    @Before
    fun setUp() {
        BenchmarkHooks.reset()
        BenchmarkHooks.enabled = true
    }

    @Test
    fun `initial snapshot is zeroed`() {
        BenchmarkHooks.reset()
        val snapshot = BenchmarkHooks.snapshot()
        assertEquals(0, snapshot.annotatorCalls)
        assertEquals(0.0, snapshot.annotatorTotalMs, 0.001)
        assertEquals(0.0, snapshot.annotatorMaxMs, 0.001)
        assertEquals(0, snapshot.selectionUpdates)
        assertEquals(0, snapshot.cacheHits)
        assertEquals(0, snapshot.cacheMisses)
    }

    @Test
    fun `recordAnnotatorTime increments call count`() {
        BenchmarkHooks.recordAnnotatorTime(1_000_000, 100)
        BenchmarkHooks.recordAnnotatorTime(2_000_000, 100)
        val snapshot = BenchmarkHooks.snapshot()
        assertEquals(2, snapshot.annotatorCalls)
    }

    @Test
    fun `recordAnnotatorTime tracks total and max`() {
        BenchmarkHooks.recordAnnotatorTime(1_000_000, 100) // 1ms
        BenchmarkHooks.recordAnnotatorTime(3_000_000, 100) // 3ms
        val snapshot = BenchmarkHooks.snapshot()
        assertEquals(4.0, snapshot.annotatorTotalMs, 0.01)
        assertEquals(3.0, snapshot.annotatorMaxMs, 0.01)
        assertEquals(2.0, snapshot.annotatorAvgMs, 0.01)
    }

    @Test
    fun `cache hit rate calculated correctly`() {
        BenchmarkHooks.recordCacheHit()
        BenchmarkHooks.recordCacheHit()
        BenchmarkHooks.recordCacheHit()
        BenchmarkHooks.recordCacheMiss()
        val snapshot = BenchmarkHooks.snapshot()
        assertEquals(0.75, snapshot.cacheHitRate, 0.001)
    }

    @Test
    fun `disabled mode does not record`() {
        BenchmarkHooks.enabled = false
        BenchmarkHooks.recordAnnotatorTime(1_000_000, 100)
        BenchmarkHooks.recordCacheHit()
        val snapshot = BenchmarkHooks.snapshot()
        assertEquals(0, snapshot.annotatorCalls)
        assertEquals(0, snapshot.cacheHits)
    }

    @Test
    fun `reset clears all metrics`() {
        BenchmarkHooks.recordAnnotatorTime(5_000_000, 100)
        BenchmarkHooks.recordCacheHit()
        BenchmarkHooks.recordSelectionUpdate(1_000_000)
        BenchmarkHooks.reset()
        val snapshot = BenchmarkHooks.snapshot()
        assertEquals(0, snapshot.annotatorCalls)
        assertEquals(0, snapshot.selectionUpdates)
        assertEquals(0, snapshot.cacheHits)
    }

    @Test
    fun `selection update metrics collected`() {
        BenchmarkHooks.recordSelectionUpdate(500_000) // 0.5ms
        BenchmarkHooks.recordSelectionUpdate(300_000) // 0.3ms
        val snapshot = BenchmarkHooks.snapshot()
        assertEquals(2, snapshot.selectionUpdates)
        assertEquals(0.8, snapshot.selectionTotalMs, 0.01)
    }
}
