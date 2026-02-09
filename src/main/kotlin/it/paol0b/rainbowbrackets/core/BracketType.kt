package it.paol0b.rainbowbrackets.core

/**
 * Enumerates the types of brackets supported by Rainbow Brackets.
 * Each type defines its opening and closing characters.
 */
enum class BracketType(val open: Char, val close: Char) {
    ROUND('(', ')'),
    SQUARE('[', ']'),
    CURLY('{', '}'),
    ANGLE('<', '>');

    companion object {
        private val openMap: Map<Char, BracketType> = entries.associateBy { it.open }
        private val closeMap: Map<Char, BracketType> = entries.associateBy { it.close }
        private val allBracketChars: Set<Char> = entries.flatMap { listOf(it.open, it.close) }.toSet()

        /**
         * Returns the [BracketType] for the given opening character, or null if not a bracket.
         */
        fun fromOpenChar(ch: Char): BracketType? = openMap[ch]

        /**
         * Returns the [BracketType] for the given closing character, or null if not a bracket.
         */
        fun fromCloseChar(ch: Char): BracketType? = closeMap[ch]

        /**
         * Returns the [BracketType] for the given character (opening or closing), or null.
         */
        fun fromChar(ch: Char): BracketType? = openMap[ch] ?: closeMap[ch]

        /**
         * Returns true if the character is any known bracket character.
         */
        fun isBracketChar(ch: Char): Boolean = ch in allBracketChars

        /**
         * Returns true if the character is an opening bracket.
         */
        fun isOpenChar(ch: Char): Boolean = ch in openMap

        /**
         * Returns true if the character is a closing bracket.
         */
        fun isCloseChar(ch: Char): Boolean = ch in closeMap
    }
}
