package it.paol0b.rainbowbrackets.util

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Font

/**
 * Utility functions for editor operations: range highlighter management,
 * selection queries, and editor state inspection.
 */
object EditorUtils {

    /**
     * Custom highlighter layer for rainbow bracket selection overlays.
     * Placed above syntax highlighting but below error stripes.
     */
    const val RAINBOW_SELECTION_LAYER: Int = HighlighterLayer.SELECTION - 1

    /**
     * Adds a background highlight to the given range in the editor.
     *
     * @param editor The target editor.
     * @param startOffset Inclusive start offset.
     * @param endOffset Exclusive end offset.
     * @param backgroundColor The background color (should be translucent).
     * @return The created [RangeHighlighter], which can be removed later.
     */
    fun addBackgroundHighlight(
        editor: Editor,
        startOffset: Int,
        endOffset: Int,
        backgroundColor: Color
    ): RangeHighlighter {
        val attrs = TextAttributes(null, backgroundColor, null, null, Font.PLAIN)
        return editor.markupModel.addRangeHighlighter(
            startOffset,
            endOffset,
            RAINBOW_SELECTION_LAYER,
            attrs,
            HighlighterTargetArea.EXACT_RANGE
        )
    }

    /**
     * Removes a previously added range highlighter from the editor.
     */
    fun removeHighlight(editor: Editor, highlighter: RangeHighlighter) {
        if (highlighter.isValid) {
            editor.markupModel.removeHighlighter(highlighter)
        }
    }

    /**
     * Removes all highlighters from a list and clears it.
     */
    fun clearHighlights(editor: Editor, highlighters: MutableList<RangeHighlighter>) {
        for (h in highlighters) {
            if (h.isValid) {
                editor.markupModel.removeHighlighter(h)
            }
        }
        highlighters.clear()
    }

    /**
     * Returns the current selection range as a Pair(start, end), or null if no selection.
     */
    fun getSelectionRange(editor: Editor): Pair<Int, Int>? {
        val model = editor.selectionModel
        if (!model.hasSelection()) return null
        return model.selectionStart to model.selectionEnd
    }

    /**
     * Returns true if the editor's document length is within safe bounds for processing.
     */
    fun isDocumentSafeForProcessing(editor: Editor, maxLength: Int = 500_000): Boolean {
        return editor.document.textLength <= maxLength
    }
}
