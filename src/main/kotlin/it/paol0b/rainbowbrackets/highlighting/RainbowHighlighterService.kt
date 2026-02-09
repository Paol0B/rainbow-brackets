package it.paol0b.rainbowbrackets.highlighting

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import it.paol0b.rainbowbrackets.core.BracketLevelCalculator
import it.paol0b.rainbowbrackets.core.BracketToken
import java.util.concurrent.ConcurrentHashMap

/**
 * Project-level service that caches bracket level computations per file.
 *
 * Invalidation occurs on file content change (detected via document modification stamp).
 * Caching dramatically reduces computation on large files since the annotator is called
 * per-element but the nesting level requires context from the whole file.
 */
class RainbowHighlighterService(private val project: Project) {

    /**
     * Cache entry: stores computed bracket tokens and the document stamp they were computed for.
     */
    private data class CacheEntry(
        val modStamp: Long,
        val tokens: List<BracketToken>,
        val tokensByOffset: Map<Int, BracketToken>
    )

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    /**
     * Returns the [BracketToken] at the given offset in the file, or null if none.
     *
     * This method is the primary entry point for the annotator. It lazily computes
     * and caches all bracket tokens for the file, then returns the one at the
     * requested offset.
     *
     * @param file The PSI file.
     * @param offset The character offset of the bracket to query.
     * @param bracketFilter Optional filter for angle bracket disambiguation.
     * @return The [BracketToken] at the given offset, or null.
     */
    fun getBracketTokenAt(
        file: PsiFile,
        offset: Int,
        bracketFilter: ((Char, Int) -> Boolean)? = null
    ): BracketToken? {
        val entry = getOrComputeCache(file, bracketFilter)
        return entry.tokensByOffset[offset]
    }

    /**
     * Returns all bracket tokens for the given file.
     * Results are cached and reused until the file changes.
     */
    fun getAllTokens(
        file: PsiFile,
        bracketFilter: ((Char, Int) -> Boolean)? = null
    ): List<BracketToken> {
        return getOrComputeCache(file, bracketFilter).tokens
    }

    /**
     * Explicitly invalidates the cache for the given file path.
     */
    fun invalidate(filePath: String) {
        cache.remove(filePath)
    }

    /**
     * Clears all cached data. Called on project disposal or settings change.
     */
    fun clearAll() {
        cache.clear()
    }

    private fun getOrComputeCache(
        file: PsiFile,
        bracketFilter: ((Char, Int) -> Boolean)?
    ): CacheEntry {
        val path = file.virtualFile?.path ?: file.name
        val currentStamp = file.modificationStamp

        val existing = cache[path]
        if (existing != null && existing.modStamp == currentStamp) {
            return existing
        }

        val text = file.text ?: return EMPTY_ENTRY
        val tokens = BracketLevelCalculator.calculateFromText(text, bracketFilter)
        val byOffset = tokens.associateBy { it.offset }
        val entry = CacheEntry(currentStamp, tokens, byOffset)
        cache[path] = entry
        return entry
    }

    companion object {
        private val EMPTY_ENTRY = CacheEntry(-1, emptyList(), emptyMap())

        fun getInstance(project: Project): RainbowHighlighterService {
            return project.getService(RainbowHighlighterService::class.java)
        }
    }
}
