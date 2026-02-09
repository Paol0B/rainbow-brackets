package it.paol0b.rainbowbrackets.colors

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import it.paol0b.rainbowbrackets.core.BracketToken
import javax.swing.Icon

/**
 * Color settings page for Rainbow Brackets.
 * Registered in plugin.xml as a `colorSettingsPage` extension.
 *
 * Allows users to customize bracket colors via:
 * Settings → Editor → Color Scheme → Rainbow Brackets
 */
class RainbowColorSettingsPage : ColorSettingsPage {

    private val descriptors: Array<AttributesDescriptor> = Array(BracketToken.MAX_LEVELS) { level ->
        AttributesDescriptor(
            RainbowColorProvider.LEVEL_DISPLAY_NAMES[level],
            RainbowColorProvider.LEVEL_KEYS[level]
        )
    }

    override fun getDisplayName(): String = "Rainbow Brackets"

    override fun getIcon(): Icon? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = descriptors

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getHighlighter(): SyntaxHighlighter = PlainSyntaxHighlighter()

    override fun getDemoText(): String {
        return buildString {
            appendLine("// Rainbow Brackets preview")
            appendLine("fun example() {")
            appendLine("    val list = listOf(")
            appendLine("        mapOf(")
            appendLine("            \"key\" to listOf(1, 2, 3)")
            appendLine("        )")
            appendLine("    )")
            appendLine("}")
        }
    }

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? {
        val map = mutableMapOf<String, TextAttributesKey>()
        for (level in 0 until BracketToken.MAX_LEVELS) {
            map["rb$level"] = RainbowColorProvider.LEVEL_KEYS[level]
        }
        return map
    }
}
