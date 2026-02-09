package it.paol0b.rainbowbrackets.util

import com.intellij.openapi.diagnostic.Logger

/**
 * Performance guard utilities for protecting the editor from expensive operations.
 *
 * Provides timeout wrappers and allocation tracking for hot paths.
 */
object PerformanceGuard {

    @PublishedApi
    internal val LOG = Logger.getInstance(PerformanceGuard::class.java)

    /** Maximum time (ms) allowed for a single annotator pass on one element. */
    const val ANNOTATOR_TIMEOUT_MS: Long = 5

    /** Maximum document size (chars) for full processing. */
    const val MAX_DOCUMENT_SIZE: Int = 500_000

    /** Maximum time (ms) allowed for selection highlight updates. */
    const val SELECTION_UPDATE_TIMEOUT_MS: Long = 2

    /**
     * Executes the given block and logs a warning if it exceeds the timeout.
     *
     * @param label A label for log messages.
     * @param timeoutMs Maximum allowed duration in milliseconds.
     * @param block The block to execute.
     * @return The result of the block, or null if an exception occurred.
     */
    inline fun <T> withTimeout(label: String, timeoutMs: Long, block: () -> T): T? {
        val start = System.nanoTime()
        return try {
            val result = block()
            val elapsed = (System.nanoTime() - start) / 1_000_000.0
            if (elapsed > timeoutMs) {
                LOG.warn("$label took ${elapsed}ms (threshold: ${timeoutMs}ms)")
            }
            result
        } catch (e: Exception) {
            LOG.error("$label failed", e)
            null
        }
    }

    /**
     * Returns true if the document size is within safe processing bounds.
     */
    fun isDocumentSafe(textLength: Int): Boolean {
        return textLength <= MAX_DOCUMENT_SIZE
    }

    /**
     * Measures execution time of a block and returns the pair (result, durationNanos).
     * Used by benchmark tests.
     */
    inline fun <T> measure(block: () -> T): Pair<T, Long> {
        val start = System.nanoTime()
        val result = block()
        val duration = System.nanoTime() - start
        return result to duration
    }
}
