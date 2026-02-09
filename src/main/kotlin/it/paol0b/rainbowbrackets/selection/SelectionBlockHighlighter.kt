package it.paol0b.rainbowbrackets.selection

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import it.paol0b.rainbowbrackets.colors.ThemeAwareColorResolver
import it.paol0b.rainbowbrackets.highlighting.EditorRangeUpdater
import it.paol0b.rainbowbrackets.highlighting.RainbowHighlighterService
import it.paol0b.rainbowbrackets.settings.RainbowSettingsState
import it.paol0b.rainbowbrackets.util.PerformanceGuard
import java.util.concurrent.ConcurrentHashMap

/**
 * Highlights the background of bracket-enclosed blocks when text is selected.
 *
 * Uses [RangeHighlighter] overlays (not Annotator) for independent lifecycle management.
 * Highlights are translucent versions of the bracket's rainbow color.
 *
 * This service is project-level and manages per-editor state.
 */
class SelectionBlockHighlighter(private val project: Project) : Disposable {

    /**
     * Per-editor range updater for managing highlight lifecycle.
     */
    private val editorUpdaters = ConcurrentHashMap<Editor, EditorRangeUpdater>()

    /**
     * Updates selection highlights for the given editor.
     *
     * Called by [RainbowSelectionListener] on selection change events.
     * Clears previous highlights and applies new ones based on the current selection.
     *
     * @param editor The editor where selection changed.
     * @param selectionStart Start offset of the selection.
     * @param selectionEnd End offset of the selection.
     */
    fun updateSelection(editor: Editor, selectionStart: Int, selectionEnd: Int) {
        val settings = RainbowSettingsState.getInstance()
        if (!settings.state.enabled || !settings.state.highlightSelection) {
            clearHighlights(editor)
            return
        }

        // Clear previous highlights
        clearHighlights(editor)

        // No selection → nothing to highlight
        if (selectionStart >= selectionEnd) return

        // Safety check
        if (!PerformanceGuard.isDocumentSafe(editor.document.textLength)) return

        PerformanceGuard.withTimeout("SelectionHighlight", PerformanceGuard.SELECTION_UPDATE_TIMEOUT_MS) {
            applySelectionHighlights(editor, selectionStart, selectionEnd)
        }
    }

    /**
     * Clears all selection highlights for the given editor.
     */
    fun clearHighlights(editor: Editor) {
        val updater = editorUpdaters[editor] ?: return
        updater.clearAll(editor)
    }

    /**
     * Clears all state. Called on disposal.
     */
    fun clearAll() {
        editorUpdaters.clear()
    }

    override fun dispose() {
        clearAll()
    }

    private fun applySelectionHighlights(editor: Editor, selectionStart: Int, selectionEnd: Int) {
        val psiFile = SelectionContextResolver.getPsiFile(editor) ?: return
        val service = RainbowHighlighterService.getInstance(project)
        val tokens = service.getAllTokens(psiFile) { char, _ -> char != '<' && char != '>' }

        // Find bracket pairs that enclose the selection
        val enclosingPairs = SelectionContextResolver.findEnclosingPairs(
            tokens, selectionStart, selectionEnd
        )

        if (enclosingPairs.isEmpty()) return

        val updater = editorUpdaters.getOrPut(editor) { EditorRangeUpdater() }

        // Highlight the innermost enclosing pair's content
        val innermost = enclosingPairs.maxByOrNull { it.openOffset }
        if (innermost != null && innermost.innerStart < innermost.innerEnd) {
            val bgColor = ThemeAwareColorResolver.resolveSelectionBackground(innermost.level)
            updater.addHighlight(editor, innermost.innerStart, innermost.innerEnd, bgColor)
        }
    }

    companion object {
        fun getInstance(project: Project): SelectionBlockHighlighter {
            return project.getService(SelectionBlockHighlighter::class.java)
        }
    }
}
