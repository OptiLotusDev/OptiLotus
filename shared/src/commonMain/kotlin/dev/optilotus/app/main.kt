package dev.optilotus.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "OptiLotus — Block Program Studio",
        state = rememberWindowState(width = 1280.dp, height = 820.dp)
    ) {
        App()
    }
}
