package it.paol0b.rainbowbrackets.core

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType

/**
 * Resolves context for angle brackets to distinguish between:
 * - Generic type parameters: `List<String>` → should be colored
 * - Comparison operators: `a < b` → should NOT be colored
 * - XML/HTML tags: `<div>` → should be colored
 *
 * This class encapsulates language-aware heuristics for angle bracket disambiguation.
 */
object BracketContextResolver {

    /**
     * Well-known PSI token type names that represent comparison/relational operators.
     * These names are used across various JetBrains language plugins.
     */
    private val OPERATOR_TOKEN_NAMES: Set<String> = setOf(
        "LT", "GT", "LE", "GE",                           // Java/Kotlin
        "LESS", "GREATER", "LESS_OR_EQUAL", "GREATER_OR_EQUAL", // Kotlin
        "LTHAN", "GTHAN",                                  // Various
        "COMP_OP",                                         // Python
        "RELATIONAL_EXPRESSION",                           // C-like
        "JS:LT", "JS:GT",                                 // JavaScript
        "SHIFT_LEFT", "SHIFT_RIGHT",                       // Bitwise shifts
        "LSHIFT", "RSHIFT",
        "LEFT_SHIFT", "RIGHT_SHIFT"
    )

    /**
     * PSI element type names that indicate a generic/type parameter context.
     */
    private val GENERIC_CONTEXT_NAMES: Set<String> = setOf(
        "TYPE_PARAMETER_LIST",
        "TYPE_ARGUMENT_LIST",
        "REFERENCE_PARAMETER_LIST",
        "TYPE_PARAMETERS",
        "TYPE_ARGUMENTS",
        "TEMPLATE_PARAMETER_LIST",
        "GENERIC_PARAMETERS",
        "TYPE_BOUND_LIST"
    )

    /**
     * PSI element type names that indicate XML/HTML tag contexts.
     */
    private val XML_TAG_NAMES: Set<String> = setOf(
        "XML_TAG",
        "HTML_TAG",
        "XML_TAG_START",
        "XML_TAG_END",
        "XML_EMPTY_TAG",
        "XML_START_TAG_START",
        "XML_END_TAG_START",
        "XML_TAG_END",
        "XML_EMPTY_ELEMENT_END"
    )

    /**
     * Determines whether the given angle bracket element should be rainbow-colored.
     *
     * @param element The PSI element representing `<` or `>`.
     * @param elementType The [IElementType] of the element.
     * @return `true` if the bracket should be colored (generic or XML tag context),
     *         `false` if it is a comparison operator and should be skipped.
     */
    fun shouldColorAngleBracket(element: PsiElement, elementType: IElementType): Boolean {
        val typeName = elementType.toString()

        // Explicitly skip known operator tokens
        if (typeName in OPERATOR_TOKEN_NAMES) {
            return false
        }

        // XML/HTML token types — always color
        if (element is XmlToken) {
            return isXmlBracketToken(element)
        }

        // Check parent context for generic type parameters
        val parent = element.parent
        if (parent != null) {
            val parentTypeName = parent.node?.elementType?.toString() ?: ""
            if (parentTypeName in GENERIC_CONTEXT_NAMES) {
                return true
            }
            if (parentTypeName in XML_TAG_NAMES) {
                return true
            }
        }

        // Heuristic: walk up the tree up to 3 levels looking for generic/XML context
        var current = parent
        repeat(3) {
            if (current == null) return@repeat
            val name = current!!.node?.elementType?.toString() ?: ""
            if (name in GENERIC_CONTEXT_NAMES || name in XML_TAG_NAMES) {
                return true
            }
            current = current!!.parent
        }

        // Default: do not color angle brackets if context is unclear
        // This avoids coloring comparison operators in ambiguous situations
        return false
    }

    /**
     * Checks if an XML token represents a bracket that should be colored.
     */
    private fun isXmlBracketToken(token: XmlToken): Boolean {
        val tokenType = token.tokenType
        return tokenType === XmlTokenType.XML_START_TAG_START ||
                tokenType === XmlTokenType.XML_END_TAG_START ||
                tokenType === XmlTokenType.XML_TAG_END ||
                tokenType === XmlTokenType.XML_EMPTY_ELEMENT_END
    }

    /**
     * Returns true if the given element type name indicates a bracket/delimiter token
     * that is NOT a comparison operator.
     */
    fun isBracketTokenType(typeName: String): Boolean {
        return typeName !in OPERATOR_TOKEN_NAMES
    }
}
