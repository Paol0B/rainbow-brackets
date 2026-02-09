package it.paol0b.rainbowbrackets.core

/**
 * Immutable representation of a single bracket token found in a document.
 *
 * @property type The bracket type (ROUND, SQUARE, CURLY, ANGLE).
 * @property offset The character offset in the document.
 * @property isOpen True if this is an opening bracket, false for closing.
 * @property level The nesting level (0-based), cycles modulo [MAX_LEVELS].
 * @property matchOffset The offset of the matching bracket, or -1 if unmatched.
 */
data class BracketToken(
    val type: BracketType,
    val offset: Int,
    val isOpen: Boolean,
    val level: Int,
    val matchOffset: Int = -1
) {
    /**
     * The color index for this bracket, which cycles through [MAX_LEVELS] colors.
     */
    val colorIndex: Int
        get() = level % MAX_LEVELS

    /**
     * Returns true if this bracket has no matching counterpart.
     */
    val isMismatched: Boolean
        get() = matchOffset < 0

    /**
     * Returns true if this bracket's range overlaps with the given selection range.
     */
    fun isInRange(rangeStart: Int, rangeEnd: Int): Boolean {
        return offset in rangeStart until rangeEnd
    }

    companion object {
        /** Number of distinct rainbow colors used for cycling. */
        const val MAX_LEVELS = 6
    }
}
