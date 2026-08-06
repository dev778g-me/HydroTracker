package com.dev.hydrotracker.data.models

/**
 * Customizable quick-add amounts for the large Home Screen widget.
 * Defaults are used until the user changes them in Settings.
 */
data class WidgetPreferences(
    val amounts: List<Int> = DEFAULT_AMOUNTS
) {
    companion object {
        val DEFAULT_AMOUNTS = listOf(250, 300, 500, 1000)

        fun default(): WidgetPreferences = WidgetPreferences(DEFAULT_AMOUNTS)
    }
}