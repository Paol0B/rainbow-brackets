package it.paol0b.rainbowbrackets.colors

import java.awt.Color

/**
 * Default color palettes for Rainbow Brackets, matching Visual Studio C# bracket colors.
 * Provides separate palettes for Light and Dark editor themes.
 */
object DefaultColorPalettes {

    /** Number of cyclic rainbow levels. */
    const val LEVEL_COUNT = 6

    /**
     * Light theme palette — Visual Studio C# style.
     * Optimized for readability on light backgrounds.
     */
    val LIGHT: List<Color> = listOf(
        Color(0xD4, 0xA0, 0x17),  // Level 0: Gold       #D4A017
        Color(0xDA, 0x70, 0xD6),  // Level 1: Orchid     #DA70D6
        Color(0x17, 0x9F, 0xFF),  // Level 2: Blue       #179FFF
        Color(0xD4, 0xA0, 0x17),  // Level 3: Gold       #D4A017 (cycle start)
        Color(0xDA, 0x70, 0xD6),  // Level 4: Orchid     #DA70D6
        Color(0x17, 0x9F, 0xFF)   // Level 5: Blue       #179FFF
    )

    /**
     * Dark theme palette — Visual Studio C# style.
     * Optimized for readability on dark backgrounds.
     */
    val DARK: List<Color> = listOf(
        Color(0xFF, 0xD7, 0x00),  // Level 0: Gold       #FFD700
        Color(0xDA, 0x70, 0xD6),  // Level 1: Orchid     #DA70D6
        Color(0x17, 0x9F, 0xFF),  // Level 2: Blue       #179FFF
        Color(0xFF, 0xD7, 0x00),  // Level 3: Gold       #FFD700 (cycle start)
        Color(0xDA, 0x70, 0xD6),  // Level 4: Orchid     #DA70D6
        Color(0x17, 0x9F, 0xFF)   // Level 5: Blue       #179FFF
    )

    /**
     * Returns the color for the given level and theme.
     *
     * @param level The nesting level (will be taken modulo [LEVEL_COUNT]).
     * @param isDark True for dark themes, false for light themes.
     * @return The [Color] for the specified level.
     */
    fun getColor(level: Int, isDark: Boolean): Color {
        val palette = if (isDark) DARK else LIGHT
        return palette[level % LEVEL_COUNT]
    }

    /**
     * Returns a translucent version of the bracket color for selection highlighting.
     *
     * @param level The nesting level.
     * @param isDark True for dark themes.
     * @param alpha Transparency value (0-255). Default is 40 (~15% opacity).
     * @return A translucent [Color] for background highlighting.
     */
    fun getSelectionColor(level: Int, isDark: Boolean, alpha: Int = 40): Color {
        val base = getColor(level, isDark)
        return Color(base.red, base.green, base.blue, alpha)
    }
}
