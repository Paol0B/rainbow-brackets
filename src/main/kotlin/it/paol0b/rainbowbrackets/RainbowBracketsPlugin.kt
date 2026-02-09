package it.paol0b.rainbowbrackets

/**
 * Rainbow Brackets plugin entry point.
 *
 * This object serves as the central namespace and version registry for the plugin.
 * All initialization is handled lazily by the JetBrains platform via plugin.xml
 * declarations — no startup activity is needed.
 */
object RainbowBracketsPlugin {

    const val PLUGIN_ID = "it.paol0b.rainbowbrackets"
    const val PLUGIN_NAME = "Fast Rainbow Brackets"
    const val VERSION = "1.0.0"
    const val AUTHOR = "Paolo Bertinetti"
}
