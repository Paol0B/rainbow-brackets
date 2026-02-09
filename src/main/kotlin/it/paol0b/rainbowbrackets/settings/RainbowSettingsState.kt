package it.paol0b.rainbowbrackets.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent application-level settings for Rainbow Brackets.
 *
 * State is saved to and loaded from the IDE's configuration directory.
 * Registered in plugin.xml as `<applicationService>`.
 */
@State(
    name = "RainbowBracketsSettings",
    storages = [Storage("rainbowBrackets.xml")]
)
class RainbowSettingsState : PersistentStateComponent<RainbowSettingsState.Config> {

    /**
     * Serializable configuration data class.
     * All fields must be `var` with defaults for XML serialization.
     */
    data class Config(
        /** Master toggle for the plugin. */
        var enabled: Boolean = true,

        /** Whether to highlight the background of selected bracket blocks. */
        var highlightSelection: Boolean = true,

        /** Whether to color round brackets (). */
        var colorRoundBrackets: Boolean = true,

        /** Whether to color square brackets []. */
        var colorSquareBrackets: Boolean = true,

        /** Whether to color curly braces {}. */
        var colorCurlyBrackets: Boolean = true,

        /** Whether to color angle brackets <> in generic/XML contexts. */
        var colorAngleBrackets: Boolean = true,

        /** Opacity for selection background highlights (0-255). */
        var selectionAlpha: Int = 40
    )

    private var config: Config = Config()

    override fun getState(): Config = config

    override fun loadState(state: Config) {
        XmlSerializerUtil.copyBean(state, config)
    }

    /**
     * Resets all settings to their default values.
     */
    fun resetDefaults() {
        config = Config()
    }

    companion object {
        /**
         * Returns the singleton instance of settings.
         */
        fun getInstance(): RainbowSettingsState {
            return ApplicationManager.getApplication().getService(RainbowSettingsState::class.java)
        }
    }
}
