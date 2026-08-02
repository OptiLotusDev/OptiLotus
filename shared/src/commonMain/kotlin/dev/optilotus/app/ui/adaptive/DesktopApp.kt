package dev.optilotus.app.ui.adaptive

import androidx.compose.runtime.Composable

/**
 * Desktop/Web canvas UI shown on big screens. On desktop (JVM) this is the spatial
 * block canvas with drag-and-drop; other platforms render a common fallback.
 */
@Composable
expect fun DesktopApp()