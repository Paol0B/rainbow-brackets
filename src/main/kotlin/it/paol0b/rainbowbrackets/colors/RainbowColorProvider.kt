package it.paol0b.rainbowbrackets.colors

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import it.paol0b.rainbowbrackets.core.BracketToken
import java.awt.Color

/**
 * Provides [TextAttributesKey] definitions for each rainbow bracket level.
 *
 * These keys integrate with the JetBrains color scheme system, allowing users
 * to customize bracket colors through Settings → Editor → Color Scheme → Rainbow Brackets.
 */
object RainbowColorProvider {

    /** Prefix for all TextAttributesKey identifiers. */
    private const val KEY_PREFIX = "RAINBOW_BRACKETS_LEVEL_"

    /**
     * TextAttributesKeys for each bracket nesting level.
     * Index 0–5 maps to the 6 cyclic rainbow colors.
     * Default colors are defined in the color scheme XML files (additionalTextAttributes).
     */
    val LEVEL_KEYS: Array<TextAttributesKey> = Array(BracketToken.MAX_LEVELS) { level ->
        TextAttributesKey.createTextAttributesKey("$KEY_PREFIX$level")
    }

    /**
     * Display names for the color settings page.
     */
    val LEVEL_DISPLAY_NAMES: Array<String> = Array(BracketToken.MAX_LEVELS) { level ->
        "Rainbow Level ${level + 1}"
    }

    /**
     * Returns the [TextAttributesKey] for the given nesting level.
     */
    fun keyForLevel(level: Int): TextAttributesKey {
        return LEVEL_KEYS[level % BracketToken.MAX_LEVELS]
    }

    /**
     * Returns the foreground [Color] for the given nesting level,
     * resolved from the current editor color scheme.
     */
    fun resolveColor(level: Int): Color {
        val scheme = EditorColorsManager.getInstance().globalScheme
        val attrs = scheme.getAttributes(keyForLevel(level))
        return attrs?.foregroundColor ?: DefaultColorPalettes.getColor(level, isDark = false)
    }

    /**
     * Returns a translucent background color for selection highlighting
     * at the given nesting level.
     *
     * @param level The bracket nesting level.
     * @param alpha Transparency (0-255). Default is 40.
     */
    fun resolveSelectionColor(level: Int, alpha: Int = 40): Color {
        val baseColor = resolveColor(level)
        return Color(baseColor.red, baseColor.green, baseColor.blue, alpha)
    }
}
