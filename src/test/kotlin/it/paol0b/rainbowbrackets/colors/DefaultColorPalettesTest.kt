package it.paol0b.rainbowbrackets.colors

import org.junit.Assert.*
import org.junit.Test
import java.awt.Color

/**
 * Unit tests for [DefaultColorPalettes].
 */
class DefaultColorPalettesTest {

    @Test
    fun `light palette has correct number of colors`() {
        assertEquals(DefaultColorPalettes.LEVEL_COUNT, DefaultColorPalettes.LIGHT.size)
    }

    @Test
    fun `dark palette has correct number of colors`() {
        assertEquals(DefaultColorPalettes.LEVEL_COUNT, DefaultColorPalettes.DARK.size)
    }

    @Test
    fun `getColor returns correct light theme color at level 0`() {
        val color = DefaultColorPalettes.getColor(0, isDark = false)
        assertEquals(Color(0xD4, 0xA0, 0x17), color)
    }

    @Test
    fun `getColor returns correct dark theme color at level 0`() {
        val color = DefaultColorPalettes.getColor(0, isDark = true)
        assertEquals(Color(0xFF, 0xD7, 0x00), color)
    }

    @Test
    fun `getColor cycles with modulo`() {
        val color6 = DefaultColorPalettes.getColor(6, isDark = false)
        val color0 = DefaultColorPalettes.getColor(0, isDark = false)
        assertEquals(color0, color6)
    }

    @Test
    fun `getSelectionColor returns translucent color`() {
        val color = DefaultColorPalettes.getSelectionColor(0, isDark = false)
        assertEquals(40, color.alpha)
        assertEquals(0xD4, color.red)
        assertEquals(0xA0, color.green)
        assertEquals(0x17, color.blue)
    }

    @Test
    fun `getSelectionColor respects custom alpha`() {
        val color = DefaultColorPalettes.getSelectionColor(0, isDark = false, alpha = 100)
        assertEquals(100, color.alpha)
    }

    @Test
    fun `light and dark palettes differ at level 0`() {
        val light = DefaultColorPalettes.getColor(0, isDark = false)
        val dark = DefaultColorPalettes.getColor(0, isDark = true)
        assertNotEquals(light, dark)
    }

    @Test
    fun `all palette colors are non-null`() {
        for (level in 0 until DefaultColorPalettes.LEVEL_COUNT) {
            assertNotNull(DefaultColorPalettes.getColor(level, isDark = false))
            assertNotNull(DefaultColorPalettes.getColor(level, isDark = true))
        }
    }
}
