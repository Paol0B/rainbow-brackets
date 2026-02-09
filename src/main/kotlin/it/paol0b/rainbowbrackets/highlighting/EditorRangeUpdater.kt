package it.paol0b.rainbowbrackets.highlighting

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.RangeHighlighter
import it.paol0b.rainbowbrackets.util.EditorUtils

/**
 * Manages incremental range updates in the editor.
 *
 * Tracks active [RangeHighlighter] instances and provides methods to
 * update only the changed ranges, avoiding full editor repaints.
 */
class EditorRangeUpdater {

    private val activeHighlighters = mutableListOf<RangeHighlighter>()

    /**
     * Clears all active range highlighters from the editor.
     */
    fun clearAll(editor: Editor) {
        EditorUtils.clearHighlights(editor, activeHighlighters)
    }

    /**
     * Adds a background highlight and tracks it for later removal.
     *
     * @param editor The target editor.
     * @param startOffset Start of the range.
     * @param endOffset End of the range.
     * @param backgroundColor The translucent background color.
     */
    fun addHighlight(
        editor: Editor,
        startOffset: Int,
        endOffset: Int,
        backgroundColor: java.awt.Color
    ) {
        val highlighter = EditorUtils.addBackgroundHighlight(
            editor, startOffset, endOffset, backgroundColor
        )
        activeHighlighters.add(highlighter)
    }

    /**
     * Returns the number of currently active highlights.
     */
    val activeCount: Int
        get() = activeHighlighters.size

    /**
     * Returns true if there are no active highlights.
     */
    val isEmpty: Boolean
        get() = activeHighlighters.isEmpty()
}
