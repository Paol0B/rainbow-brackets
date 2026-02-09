package it.paol0b.rainbowbrackets.selection

import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.project.Project

/**
 * Declarative selection listener registered in plugin.xml via `<projectListeners>`.
 *
 * Lazily instantiated on the first selection event. Delegates to
 * [SelectionBlockHighlighter] for applying translucent bracket background highlights.
 *
 * This listener is automatically cleaned up on project close.
 */
class RainbowSelectionListener(private val project: Project) : SelectionListener {

    override fun selectionChanged(e: SelectionEvent) {
        if (project.isDisposed) return

        val editor = e.editor
        val highlighter = SelectionBlockHighlighter.getInstance(project)

        val selectionModel = editor.selectionModel
        if (selectionModel.hasSelection()) {
            highlighter.updateSelection(
                editor,
                selectionModel.selectionStart,
                selectionModel.selectionEnd
            )
        } else {
            highlighter.clearHighlights(editor)
        }
    }
}
