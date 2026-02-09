package it.paol0b.rainbowbrackets.colors

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor
import it.paol0b.rainbowbrackets.core.BracketToken
import java.awt.Color
import java.awt.Font

/**
 * Resolves bracket colors based on the current editor theme (Light / Dark).
 *
 * This resolver delegates to the [EditorColorsScheme] system, which automatically
 * handles theme switching. It provides a single API surface for both bracket
 * foreground coloring and selection background highlighting.
 */
object ThemeAwareColorResolver {

    /**
     * Returns the resolved [TextAttributes] for the given bracket level,
     * using the current global color scheme.
     */
    fun resolveAttributes(level: Int): TextAttributes {
        val scheme = EditorColorsManager.getInstance().globalScheme
        val key = RainbowColorProvider.keyForLevel(level)
        return scheme.getAttributes(key) ?: createFallbackAttributes(level)
    }

    /**
     * Returns the resolved [TextAttributes] for a specific editor instance.
     */
    fun resolveAttributes(level: Int, editor: Editor): TextAttributes {
        val scheme = editor.colorsScheme
        val key = RainbowColorProvider.keyForLevel(level)
        return scheme.getAttributes(key) ?: createFallbackAttributes(level)
    }

    /**
     * Returns the foreground color for the given level in the current theme.
     */
    fun resolveColor(level: Int): Color {
        val attrs = resolveAttributes(level)
        return attrs.foregroundColor ?: getFallbackColor(level)
    }

    /**
     * Returns a translucent background color for selection overlay at the given level.
     *
     * @param level The nesting level.
     * @param alpha Transparency (0-255). Default: 40 (~15%).
     */
    fun resolveSelectionBackground(level: Int, alpha: Int = 40): Color {
        val base = resolveColor(level)
        return Color(base.red, base.green, base.blue, alpha)
    }

    /**
     * Determines if the current global scheme is a dark theme.
     */
    fun isDarkTheme(): Boolean {
        return !JBColor.isBright()
    }

    /**
     * Creates fallback attributes when the color scheme doesn't define them.
     */
    private fun createFallbackAttributes(level: Int): TextAttributes {
        val color = getFallbackColor(level)
        return TextAttributes(color, null, null, null, Font.PLAIN)
    }

    /**
     * Returns the fallback color from [DefaultColorPalettes].
     */
    private fun getFallbackColor(level: Int): Color {
        return DefaultColorPalettes.getColor(level % BracketToken.MAX_LEVELS, isDarkTheme())
    }
}
