package it.paol0b.rainbowbrackets.selection

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import it.paol0b.rainbowbrackets.core.BracketToken

/**
 * Resolves which bracket pairs are affected by the current text selection.
 *
 * This class efficiently determines which bracket pairs enclose or are enclosed by
 * the selection range, providing data for the [SelectionBlockHighlighter].
 */
object SelectionContextResolver {

    /**
     * Information about a bracket pair affected by a selection.
     *
     * @property openOffset Offset of the opening bracket.
     * @property closeOffset Offset of the closing bracket.
     * @property level The nesting level (0-based, modulo MAX_LEVELS).
     * @property innerStart The first character after the opening bracket.
     * @property innerEnd The last character before the closing bracket.
     */
    data class AffectedPair(
        val openOffset: Int,
        val closeOffset: Int,
        val level: Int,
        val innerStart: Int,
        val innerEnd: Int
    )

    /**
     * Finds all bracket pairs that enclose the current selection.
     *
     * A bracket pair "encloses" the selection if:
     * - The opening bracket is at or before the selection start
     * - The closing bracket is at or after the selection end
     *
     * @param tokens List of all bracket tokens in the file.
     * @param selectionStart The start offset of the selection.
     * @param selectionEnd The end offset of the selection.
     * @return List of [AffectedPair] instances for enclosing brackets.
     */
    fun findEnclosingPairs(
        tokens: List<BracketToken>,
        selectionStart: Int,
        selectionEnd: Int
    ): List<AffectedPair> {
        if (tokens.isEmpty() || selectionStart >= selectionEnd) return emptyList()

        val result = mutableListOf<AffectedPair>()

        for (token in tokens) {
            // Only process opening brackets that have a match
            if (!token.isOpen || token.isMismatched) continue

            val openOffset = token.offset
            val closeOffset = token.matchOffset

            // Check if this pair encloses the selection
            if (openOffset <= selectionStart && closeOffset >= selectionEnd - 1) {
                result.add(
                    AffectedPair(
                        openOffset = openOffset,
                        closeOffset = closeOffset,
                        level = token.colorIndex,
                        innerStart = openOffset + 1,
                        innerEnd = closeOffset
                    )
                )
            }
        }

        return result
    }

    /**
     * Finds the innermost bracket pair that encloses the caret position.
     *
     * @param tokens List of all bracket tokens.
     * @param caretOffset The caret position.
     * @return The innermost enclosing [AffectedPair], or null.
     */
    fun findInnermostEnclosingPair(
        tokens: List<BracketToken>,
        caretOffset: Int
    ): AffectedPair? {
        var best: AffectedPair? = null
        var bestSize = Int.MAX_VALUE

        for (token in tokens) {
            if (!token.isOpen || token.isMismatched) continue

            val openOffset = token.offset
            val closeOffset = token.matchOffset
            val size = closeOffset - openOffset

            if (openOffset < caretOffset && closeOffset > caretOffset && size < bestSize) {
                best = AffectedPair(
                    openOffset = openOffset,
                    closeOffset = closeOffset,
                    level = token.colorIndex,
                    innerStart = openOffset + 1,
                    innerEnd = closeOffset
                )
                bestSize = size
            }
        }

        return best
    }

    /**
     * Gets the PSI file for the given editor, if available.
     */
    fun getPsiFile(editor: Editor): PsiFile? {
        val project = editor.project ?: return null
        return PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
    }
}
