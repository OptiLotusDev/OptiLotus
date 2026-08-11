package dev.optilotus.app.ui.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.window.core.layout.WindowSizeClass

enum class UIMode { DESKTOP, MOBILE }

@Composable
fun rememberUIMode(): UIMode {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    // Big screens (>=840dp width) always get the Desktop/Web canvas UI.
    val isExpandedWidth =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    // Compact width (<600dp) is a small screen: mobile UI.
    val isCompactWidth =
        !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    // Medium width (600-840dp): only use the mobile UI when vertical (short height).
    val isShortHeight =
        !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

    return when {
        isExpandedWidth -> UIMode.DESKTOP
        isCompactWidth -> UIMode.MOBILE
        isShortHeight -> UIMode.MOBILE
        else -> UIMode.DESKTOP
    }
}