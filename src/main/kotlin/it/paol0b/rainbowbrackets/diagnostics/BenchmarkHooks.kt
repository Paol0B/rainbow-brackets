package it.paol0b.rainbowbrackets.diagnostics

import com.intellij.openapi.diagnostic.Logger
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong

/**
 * Instrumentation hooks for measuring Rainbow Brackets performance.
 *
 * Collects metrics on annotator execution, selection updates, and cache efficiency.
 * Used by benchmark tests to validate performance targets.
 *
 * Thread-safe: all operations use atomic variables or concurrent collections.
 */
object BenchmarkHooks {

    private val LOG = Logger.getInstance(BenchmarkHooks::class.java)

    /** Whether benchmarking is active. Disabled by default to avoid overhead. */
    @Volatile
    var enabled: Boolean = false

    // --- Annotator metrics ---
    private val annotatorCallCount = AtomicLong(0)
    private val annotatorTotalNanos = AtomicLong(0)
    private val annotatorMaxNanos = AtomicLong(0)

    // --- Selection metrics ---
    private val selectionUpdateCount = AtomicLong(0)
    private val selectionTotalNanos = AtomicLong(0)

    // --- Cache metrics ---
    private val cacheHitCount = AtomicLong(0)
    private val cacheMissCount = AtomicLong(0)

    // --- Sample recordings (last N entries) ---
    private val recentAnnotatorTimesMs = ConcurrentLinkedQueue<Double>()
    private const val MAX_RECENT_SAMPLES = 1000

    /**
     * Records the execution time of a single annotator call.
     *
     * @param durationNanos Duration in nanoseconds.
     * @param fileSize The size of the file in characters.
     */
    fun recordAnnotatorTime(durationNanos: Long, fileSize: Int) {
        if (!enabled) return
        annotatorCallCount.incrementAndGet()
        annotatorTotalNanos.addAndGet(durationNanos)
        annotatorMaxNanos.updateAndGet { maxOf(it, durationNanos) }

        val ms = durationNanos / 1_000_000.0
        if (recentAnnotatorTimesMs.size < MAX_RECENT_SAMPLES) {
            recentAnnotatorTimesMs.add(ms)
        }
    }

    /**
     * Records the execution time of a selection highlight update.
     */
    fun recordSelectionUpdate(durationNanos: Long) {
        if (!enabled) return
        selectionUpdateCount.incrementAndGet()
        selectionTotalNanos.addAndGet(durationNanos)
    }

    /**
     * Records a cache hit.
     */
    fun recordCacheHit() {
        if (!enabled) return
        cacheHitCount.incrementAndGet()
    }

    /**
     * Records a cache miss.
     */
    fun recordCacheMiss() {
        if (!enabled) return
        cacheMissCount.incrementAndGet()
    }

    /**
     * Returns a snapshot of current metrics.
     */
    fun snapshot(): BenchmarkSnapshot {
        return BenchmarkSnapshot(
            annotatorCalls = annotatorCallCount.get(),
            annotatorTotalMs = annotatorTotalNanos.get() / 1_000_000.0,
            annotatorMaxMs = annotatorMaxNanos.get() / 1_000_000.0,
            annotatorAvgMs = if (annotatorCallCount.get() > 0)
                (annotatorTotalNanos.get() / 1_000_000.0) / annotatorCallCount.get()
            else 0.0,
            selectionUpdates = selectionUpdateCount.get(),
            selectionTotalMs = selectionTotalNanos.get() / 1_000_000.0,
            cacheHits = cacheHitCount.get(),
            cacheMisses = cacheMissCount.get(),
            cacheHitRate = if (cacheHitCount.get() + cacheMissCount.get() > 0)
                cacheHitCount.get().toDouble() / (cacheHitCount.get() + cacheMissCount.get())
            else 0.0
        )
    }

    /**
     * Resets all metrics to zero.
     */
    fun reset() {
        annotatorCallCount.set(0)
        annotatorTotalNanos.set(0)
        annotatorMaxNanos.set(0)
        selectionUpdateCount.set(0)
        selectionTotalNanos.set(0)
        cacheHitCount.set(0)
        cacheMissCount.set(0)
        recentAnnotatorTimesMs.clear()
    }

    /**
     * Logs the current metrics at INFO level.
     */
    fun logSnapshot() {
        val s = snapshot()
        LOG.info(
            "RainbowBrackets Benchmark: " +
                    "annotator=${s.annotatorCalls} calls, " +
                    "avg=${String.format("%.3f", s.annotatorAvgMs)}ms, " +
                    "max=${String.format("%.3f", s.annotatorMaxMs)}ms, " +
                    "selection=${s.selectionUpdates} updates, " +
                    "cache hit rate=${String.format("%.1f", s.cacheHitRate * 100)}%"
        )
    }

    /**
     * Immutable snapshot of benchmark metrics.
     */
    data class BenchmarkSnapshot(
        val annotatorCalls: Long,
        val annotatorTotalMs: Double,
        val annotatorMaxMs: Double,
        val annotatorAvgMs: Double,
        val selectionUpdates: Long,
        val selectionTotalMs: Double,
        val cacheHits: Long,
        val cacheMisses: Long,
        val cacheHitRate: Double
    )
}
