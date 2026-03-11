package com.vahitkeskin.fencecalculator.util

import androidx.navigation.NavController

/**
 * Utility for safe navigation operations.
 */
object NavigationUtils {
    private var lastClickTime: Long = 0
    private const val DEBOUNCE_INTERVAL = 500L // milliseconds

    /**
     * Executes the [action] only if the [DEBOUNCE_INTERVAL] has passed since the last click.
     */
    fun safeClick(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
            lastClickTime = currentTime
            action()
        }
    }

    /**
     * Safely pops the back stack of the [NavController].
     * Checks if there is more than one entry in the back stack to avoid popping the root.
     */
    fun NavController.safePopBackStack() {
        safeClick {
            if (previousBackStackEntry != null) {
                popBackStack()
            }
        }
    }
}
