package com.mindsparq.os

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(
        width = 1360.dp,
        height = 860.dp,
        placement = WindowPlacement.Floating,
    )

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "MindSparQ OS",
    ) {
        MindSparqApp(isDesktop = true)
    }
}
