package it.paol0b.rainbowbrackets.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlToken
import it.paol0b.rainbowbrackets.colors.RainbowColorProvider
import it.paol0b.rainbowbrackets.core.BracketContextResolver
import it.paol0b.rainbowbrackets.core.BracketType
import it.paol0b.rainbowbrackets.settings.RainbowSettingsState
import it.paol0b.rainbowbrackets.util.PerformanceGuard
import it.paol0b.rainbowbrackets.util.PsiUtils

/**
 * Element-level annotator for rainbow bracket coloring.
 *
 * Registered in plugin.xml with `language=""` to handle ALL languages.
 * Runs in parallel since IntelliJ 2024.1+ — each element is processed independently.
 *
 * This annotator:
 * 1. Checks if the PSI element is a bracket token
 * 2. For angle brackets, resolves context (generic vs comparison operator)
 * 3. Computes nesting level via the cached [RainbowHighlighterService]
 * 4. Applies the appropriate [TextAttributesKey] for the level
 */
class RainbowBracketAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Check if plugin is enabled
        val settings = RainbowSettingsState.getInstance()
        if (!settings.state.enabled) return

        // Safety: skip very large documents
        val file = element.containingFile ?: return
        val textLength = file.textLength
        if (!PerformanceGuard.isDocumentSafe(textLength)) return

        // Determine if this element is a bracket
        val bracketChar = determineBracketChar(element) ?: return

        // For angle brackets, check context to avoid coloring comparison operators
        if (bracketChar == '<' || bracketChar == '>') {
            val elementType = element.node?.elementType ?: return
            if (!BracketContextResolver.shouldColorAngleBracket(element, elementType)) {
                return
            }
        }

        // Get the project-level service for cached level computation
        val project = element.project
        val service = RainbowHighlighterService.getInstance(project)

        // Create a bracket filter that excludes non-bracket angle brackets
        val bracketFilter = createBracketFilter(file)

        // Retrieve or compute the bracket token at this offset
        val offset = element.textRange.startOffset
        val bracketToken = service.getBracketTokenAt(file, offset, bracketFilter) ?: return

        // Apply the rainbow color annotation
        val key = RainbowColorProvider.keyForLevel(bracketToken.colorIndex)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element.textRange)
            .textAttributes(key)
            .create()
    }

    /**
     * Determines the bracket character for the given element, or null if not a bracket.
     */
    private fun determineBracketChar(element: PsiElement): Char? {
        // XML tokens have special handling
        if (element is XmlToken) {
            return PsiUtils.getXmlBracketChar(element)
        }

        // Check standard bracket text
        val node = element.node ?: return null
        val text = node.text
        if (text.length == 1 && BracketType.isBracketChar(text[0])) {
            return text[0]
        }

        return null
    }

    /**
     * Creates a filter for the bracket level calculator that excludes
     * angle brackets in non-generic contexts. This filter uses simple
     * text heuristics since we don't have PSI context during text scanning.
     *
     * For Text-based scanning in BracketLevelCalculator, we only include
     * round, square, and curly brackets unconditionally. Angle brackets
     * require PSI context which is handled separately.
     */
    private fun createBracketFilter(file: com.intellij.psi.PsiFile): (Char, Int) -> Boolean {
        return { char, _ ->
            // Always include round, square, curly brackets
            // Exclude angle brackets from the text-based scan — they're handled
            // via PSI-aware context resolution in the annotator
            char != '<' && char != '>'
        }
    }

    /**
     * Extension to get XML bracket char from utility, accessible to the annotator.
     */
    private fun PsiUtils.getXmlBracketChar(token: XmlToken): Char? {
        return PsiUtils.getBracketChar(token)
    }
}
