package it.paol0b.rainbowbrackets.settings

import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSlider

/**
 * UI component for the Rainbow Brackets settings panel.
 *
 * Contains checkboxes for enabling/disabling features and a slider
 * for selection highlight opacity. Used by [RainbowSettingsConfigurable].
 */
class RainbowSettingsComponent {

    val panel: JPanel
    val enabledCheckbox = JBCheckBox("Enable Rainbow Brackets")
    val highlightSelectionCheckbox = JBCheckBox("Highlight selected bracket blocks")
    val colorRoundCheckbox = JBCheckBox("Color round brackets ()")
    val colorSquareCheckbox = JBCheckBox("Color square brackets []")
    val colorCurlyCheckbox = JBCheckBox("Color curly braces {}")
    val colorAngleCheckbox = JBCheckBox("Color angle brackets <> (generics, XML tags)")
    val selectionAlphaSlider = JSlider(0, 100, 16).apply {
        majorTickSpacing = 25
        minorTickSpacing = 5
        paintTicks = true
        paintLabels = true
        toolTipText = "Selection highlight opacity (%)"
    }

    init {
        panel = FormBuilder.createFormBuilder()
            .addComponent(enabledCheckbox)
            .addSeparator()
            .addComponent(colorRoundCheckbox)
            .addComponent(colorSquareCheckbox)
            .addComponent(colorCurlyCheckbox)
            .addComponent(colorAngleCheckbox)
            .addSeparator()
            .addComponent(highlightSelectionCheckbox)
            .addLabeledComponent("Selection opacity:", selectionAlphaSlider)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    /**
     * Returns the preferred focusable component for keyboard navigation.
     */
    val preferredFocusedComponent: JComponent
        get() = enabledCheckbox
}
