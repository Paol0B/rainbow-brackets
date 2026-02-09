package it.paol0b.rainbowbrackets.core

/**
 * Pure-logic calculator for bracket nesting levels.
 * No IDE dependencies — fully testable in isolation.
 *
 * Given a sequence of bracket characters with their offsets, computes the
 * nesting level for each bracket and matches opening/closing pairs.
 */
object BracketLevelCalculator {

    /**
     * Represents a bracket character found at a specific offset.
     */
    data class BracketChar(val char: Char, val offset: Int)

    /**
     * Processes a sequence of bracket characters and returns [BracketToken] instances
     * with computed nesting levels and pair matching.
     *
     * The algorithm uses a per-type stack to correctly handle interleaved bracket types
     * (e.g., `({[]})`). Each bracket type has an independent depth counter that cycles
     * modulo [BracketToken.MAX_LEVELS].
     *
     * @param brackets Sequence of bracket characters in document order.
     * @return List of [BracketToken] with levels and match offsets computed.
     */
    fun calculateLevels(brackets: List<BracketChar>): List<BracketToken> {
        // Per-type stack: stores (index in result list, offset) for unmatched openers
        val stacks = BracketType.entries.associateWith { mutableListOf<StackEntry>() }
        val result = mutableListOf<BracketToken>()

        for (bracket in brackets) {
            val ch = bracket.char
            val offset = bracket.offset

            val openType = BracketType.fromOpenChar(ch)
            if (openType != null) {
                val stack = stacks.getValue(openType)
                val level = stack.size % BracketToken.MAX_LEVELS
                val token = BracketToken(
                    type = openType,
                    offset = offset,
                    isOpen = true,
                    level = level
                )
                val index = result.size
                result.add(token)
                stack.add(StackEntry(index, offset))
                continue
            }

            val closeType = BracketType.fromCloseChar(ch)
            if (closeType != null) {
                val stack = stacks.getValue(closeType)
                if (stack.isNotEmpty()) {
                    val opener = stack.removeAt(stack.lastIndex)
                    val level = opener.index.let { result[it].level }
                    val token = BracketToken(
                        type = closeType,
                        offset = offset,
                        isOpen = false,
                        level = level,
                        matchOffset = opener.offset
                    )
                    // Update the opener with the match offset
                    result[opener.index] = result[opener.index].copy(matchOffset = offset)
                    result.add(token)
                } else {
                    // Unmatched closer
                    val token = BracketToken(
                        type = closeType,
                        offset = offset,
                        isOpen = false,
                        level = 0,
                        matchOffset = -1
                    )
                    result.add(token)
                }
            }
        }

        return result
    }

    /**
     * Convenience method: computes all bracket tokens from raw text.
     * Extracts bracket characters, then delegates to [calculateLevels].
     *
     * @param text The source text to analyze.
     * @param bracketFilter Optional filter to exclude certain brackets
     *        (e.g., angle brackets in non-generic contexts).
     * @return List of [BracketToken] with levels and match offsets.
     */
    fun calculateFromText(
        text: CharSequence,
        bracketFilter: ((Char, Int) -> Boolean)? = null
    ): List<BracketToken> {
        val brackets = mutableListOf<BracketChar>()
        for (i in text.indices) {
            val ch = text[i]
            if (BracketType.isBracketChar(ch)) {
                if (bracketFilter == null || bracketFilter(ch, i)) {
                    brackets.add(BracketChar(ch, i))
                }
            }
        }
        return calculateLevels(brackets)
    }

    private data class StackEntry(val index: Int, val offset: Int)
}
