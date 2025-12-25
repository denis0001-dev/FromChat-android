package com.pr0gramm3r101.utils

import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * Navigates to the specified route and clears the entire back stack.
 * This ensures the destination becomes the new root of the navigation stack.
 *
 * @param route The destination route to navigate to
 * @param launchSingleTop If true, prevents multiple instances of the same destination
 */
fun NavController.navigateAndWipeBackStack(route: String, launchSingleTop: Boolean = true) {
    // First, pop all previous entries (keeping current screen for now)
    while (previousBackStackEntry != null) {
        if (!popBackStack()) {
            break
        }
    }

    // Get current route to pop it with the navigation
    val currentRoute = currentBackStackEntry?.destination?.route

    // Navigate to the new route, removing the current screen as well
    navigate(route) {
        if (currentRoute != null && currentRoute != route) {
            popUpTo(currentRoute) {
                inclusive = true
            }
        }
        this.launchSingleTop = launchSingleTop
    }
}

inline fun Modifier.conditional(
    condition: Boolean,
    `else`: Modifier.(Modifier) -> Modifier = { Modifier },
    `if`: Modifier.(Modifier) -> Modifier
) = if (condition) {
    this + `if`(this)
} else {
    this + `else`(this)
}