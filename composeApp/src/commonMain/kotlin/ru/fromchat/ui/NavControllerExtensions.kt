package ru.fromchat.ui

import androidx.navigation.NavController

/**
 * Clears the entire back stack except the current screen
 */
fun NavController.wipeBackStack() {
    // Keep popping until there are no previous entries
    var entry = previousBackStackEntry
    while (entry != null) {
        val destinationId = entry.destination.id
        val success = popBackStack(destinationId, inclusive = true)
        if (!success) break
        // Check again after popping
        entry = previousBackStackEntry
    }
}

