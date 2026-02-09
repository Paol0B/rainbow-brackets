package it.paol0b.rainbowbrackets.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * Settings configurable for Rainbow Brackets.
 * Registered in plugin.xml as `<applicationConfigurable>` under `parentId="tools"`.
 *
 * Accessible via: Settings → Tools → Rainbow Brackets
 */
class RainbowSettingsConfigurable : Configurable {

    private var component: RainbowSettingsComponent? = null

    override fun getDisplayName(): String = "Rainbow Brackets"

    override fun getPreferredFocusedComponent(): JComponent? {
        return component?.preferredFocusedComponent
    }

    override fun createComponent(): JComponent? {
        component = RainbowSettingsComponent()
        return component?.panel
    }

    override fun isModified(): Boolean {
        val comp = component ?: return false
        val config = RainbowSettingsState.getInstance().state
        return comp.enabledCheckbox.isSelected != config.enabled ||
                comp.highlightSelectionCheckbox.isSelected != config.highlightSelection ||
                comp.colorRoundCheckbox.isSelected != config.colorRoundBrackets ||
                comp.colorSquareCheckbox.isSelected != config.colorSquareBrackets ||
                comp.colorCurlyCheckbox.isSelected != config.colorCurlyBrackets ||
                comp.colorAngleCheckbox.isSelected != config.colorAngleBrackets ||
                alphaFromSlider(comp.selectionAlphaSlider.value) != config.selectionAlpha
    }

    override fun apply() {
        val comp = component ?: return
        val config = RainbowSettingsState.getInstance().state
        config.enabled = comp.enabledCheckbox.isSelected
        config.highlightSelection = comp.highlightSelectionCheckbox.isSelected
        config.colorRoundBrackets = comp.colorRoundCheckbox.isSelected
        config.colorSquareBrackets = comp.colorSquareCheckbox.isSelected
        config.colorCurlyBrackets = comp.colorCurlyCheckbox.isSelected
        config.colorAngleBrackets = comp.colorAngleCheckbox.isSelected
        config.selectionAlpha = alphaFromSlider(comp.selectionAlphaSlider.value)
    }

    override fun reset() {
        val comp = component ?: return
        val config = RainbowSettingsState.getInstance().state
        comp.enabledCheckbox.isSelected = config.enabled
        comp.highlightSelectionCheckbox.isSelected = config.highlightSelection
        comp.colorRoundCheckbox.isSelected = config.colorRoundBrackets
        comp.colorSquareCheckbox.isSelected = config.colorSquareBrackets
        comp.colorCurlyCheckbox.isSelected = config.colorCurlyBrackets
        comp.colorAngleCheckbox.isSelected = config.colorAngleBrackets
        comp.selectionAlphaSlider.value = sliderFromAlpha(config.selectionAlpha)
    }

    override fun disposeUIResources() {
        component = null
    }

    /**
     * Converts slider value (0-100 percent) to alpha (0-255).
     */
    private fun alphaFromSlider(sliderValue: Int): Int {
        return (sliderValue * 255 / 100).coerceIn(0, 255)
    }

    /**
     * Converts alpha (0-255) to slider value (0-100 percent).
     */
    private fun sliderFromAlpha(alpha: Int): Int {
        return (alpha * 100 / 255).coerceIn(0, 100)
    }
}
