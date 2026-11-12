package com.narkolep.skkimmer.ui.components.colorpicker

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt


fun Color.toHsv(): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(
        toArgb(),
        hsv
    )
    return hsv
}

fun Color.toHex(): String {
    return String.format(
        "#%06X",
        0xFFFFFF and toArgb()
    )
}

fun String.toComposeColor(): Color? {
    val hex = removePrefix("#")

    if (!hex.matches(Regex("[0-9a-fA-F]{6}"))) {
        return null
    }

    return try {
        Color(
            "#$hex".toColorInt()
        )
    } catch (_: IllegalArgumentException) {
        null
    }
}