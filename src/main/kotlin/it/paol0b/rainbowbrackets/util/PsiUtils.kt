package it.paol0b.rainbowbrackets.util

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType

/**
 * Language-agnostic PSI utility functions for bracket detection and matching.
 *
 * Works across all JetBrains-supported languages by inspecting token text
 * and element types rather than relying on language-specific PSI classes.
 */
object PsiUtils {

    /** Characters recognized as bracket delimiters. */
    private val BRACKET_CHARS = setOf('(', ')', '{', '}', '[', ']', '<', '>')

    /** Common token type names for round brackets across languages. */
    private val ROUND_OPEN_NAMES = setOf("LPARENTH", "LPAR", "LPAREN", "(", "JS:LPAR")
    private val ROUND_CLOSE_NAMES = setOf("RPARENTH", "RPAR", "RPAREN", ")", "JS:RPAR")

    /** Common token type names for square brackets. */
    private val SQUARE_OPEN_NAMES = setOf("LBRACKET", "LBRACK", "[", "JS:LBRACKET")
    private val SQUARE_CLOSE_NAMES = setOf("RBRACKET", "RBRACK", "]", "JS:RBRACKET")

    /** Common token type names for curly braces. */
    private val CURLY_OPEN_NAMES = setOf("LBRACE", "LCURLY", "{", "JS:LBRACE")
    private val CURLY_CLOSE_NAMES = setOf("RBRACE", "RCURLY", "}", "JS:RBRACE")

    /** Common token type names for angle brackets in generic/type contexts. */
    private val ANGLE_OPEN_NAMES = setOf("LT", "LESS", "<", "JS:LT")
    private val ANGLE_CLOSE_NAMES = setOf("GT", "GREATER", ">", "JS:GT")

    /**
     * XML/HTML token types that represent tag delimiters.
     * These are always colored regardless of operator disambiguation.
     */
    private val XML_BRACKET_TYPES = setOf(
        XmlTokenType.XML_START_TAG_START,
        XmlTokenType.XML_END_TAG_START,
        XmlTokenType.XML_TAG_END,
        XmlTokenType.XML_EMPTY_ELEMENT_END
    )

    /**
     * Determines if a PSI element represents a bracket that should be rainbow-colored.
     *
     * @param element The PSI element to check.
     * @return The bracket character if this is a colorable bracket, null otherwise.
     */
    fun getBracketChar(element: PsiElement): Char? {
        val node = element.node ?: return null
        val text = node.text

        // XML tokens have special handling
        if (element is XmlToken) {
            return getXmlBracketChar(element)
        }

        // Single-character bracket tokens
        if (text.length == 1 && text[0] in BRACKET_CHARS) {
            return text[0]
        }

        // Multi-character tokens that represent brackets (e.g., "/>" for XML)
        return null
    }

    /**
     * Returns the bracket character for an XML token, if applicable.
     */
    private fun getXmlBracketChar(token: XmlToken): Char? {
        return when (token.tokenType) {
            XmlTokenType.XML_START_TAG_START -> '<'
            XmlTokenType.XML_END_TAG_START -> '<'
            XmlTokenType.XML_TAG_END -> '>'
            XmlTokenType.XML_EMPTY_ELEMENT_END -> '>'
            else -> null
        }
    }

    /**
     * Returns true if the element's token type name indicates a round bracket.
     */
    fun isRoundBracket(elementType: IElementType): Boolean {
        val name = elementType.toString()
        return name in ROUND_OPEN_NAMES || name in ROUND_CLOSE_NAMES
    }

    /**
     * Returns true if the element's token type name indicates a square bracket.
     */
    fun isSquareBracket(elementType: IElementType): Boolean {
        val name = elementType.toString()
        return name in SQUARE_OPEN_NAMES || name in SQUARE_CLOSE_NAMES
    }

    /**
     * Returns true if the element's token type name indicates a curly brace.
     */
    fun isCurlyBracket(elementType: IElementType): Boolean {
        val name = elementType.toString()
        return name in CURLY_OPEN_NAMES || name in CURLY_CLOSE_NAMES
    }

    /**
     * Returns true if the element is an XML bracket delimiter.
     */
    fun isXmlBracket(element: PsiElement): Boolean {
        if (element !is XmlToken) return false
        return element.tokenType in XML_BRACKET_TYPES
    }

    /**
     * Counts the nesting level of a bracket element by walking up its siblings
     * and counting matching open brackets of the same type.
     *
     * @param element The bracket PSI element.
     * @param bracketChar The bracket character ('(', '{', etc.).
     * @return The 0-based nesting level.
     */
    fun computeNestingLevel(element: PsiElement, bracketChar: Char): Int {
        val file = element.containingFile ?: return 0
        val offset = element.textRange.startOffset
        val text = file.text ?: return 0

        return computeNestingLevelFromText(text, offset, bracketChar)
    }

    /**
     * Computes the nesting level by scanning text from the start of the file
     * up to the given offset, counting matching brackets.
     *
     * This is a pure-text computation (no PSI traversal) for maximum performance.
     */
    fun computeNestingLevelFromText(text: CharSequence, offset: Int, bracketChar: Char): Int {
        val isOpen = bracketChar == '(' || bracketChar == '{' ||
                bracketChar == '[' || bracketChar == '<'
        val openChar: Char
        val closeChar: Char

        when (bracketChar) {
            '(', ')' -> { openChar = '('; closeChar = ')' }
            '{', '}' -> { openChar = '{'; closeChar = '}' }
            '[', ']' -> { openChar = '['; closeChar = ']' }
            '<', '>' -> { openChar = '<'; closeChar = '>' }
            else -> return 0
        }

        var depth = 0
        val end = if (isOpen) offset else offset + 1
        for (i in 0 until end.coerceAtMost(text.length)) {
            when (text[i]) {
                openChar -> depth++
                closeChar -> if (depth > 0) depth--
            }
        }

        // For open brackets, the current depth IS the level.
        // For close brackets, we subtract 1 because we already counted the match.
        return if (isOpen) {
            depth
        } else {
            depth
        }
    }

    /**
     * Finds all leaf PSI elements in a file within the given text range.
     * Used for efficiently scanning a subset of the document.
     *
     * @param file The PSI file.
     * @param startOffset Inclusive start offset.
     * @param endOffset Exclusive end offset.
     * @return Sequence of leaf PSI elements in the range.
     */
    fun findLeavesInRange(file: PsiFile, startOffset: Int, endOffset: Int): Sequence<PsiElement> {
        return sequence {
            var element = file.findElementAt(startOffset)
            while (element != null && element.textRange.startOffset < endOffset) {
                yield(element)
                element = nextLeaf(element)
            }
        }
    }

    /**
     * Moves to the next leaf element in the PSI tree.
     */
    private fun nextLeaf(element: PsiElement): PsiElement? {
        var current = element
        // Try next sibling
        while (current.nextSibling == null) {
            current = current.parent ?: return null
        }
        current = current.nextSibling
        // Descend to first leaf
        while (current.firstChild != null) {
            current = current.firstChild
        }
        return current
    }
}
