package com.narkolep.skkimmer.keyboard.ui.theme

import androidx.compose.ui.graphics.Color

data class KeyboardColors(
    val background: Color,
    val keyBackground: Color,
    val keyText: Color,
    val flickKeyBackground: Color,
    val specialKeyBackground: Color,
    val specialKeyText: Color,
)

fun keyboardColors(selectedColor: HSVColor, isDark: Boolean? = null): KeyboardColors {
    if (isDark == true) {
        val backgroundHSV: HSVColor = selectedColor.copy(
            saturation = selectedColor.saturation.times(0.25f),
            value = 0.12f
        )
        val keyBackgroundHSV: HSVColor = selectedColor.copy(
            hue = (((selectedColor.hue + 15f) % 360f) + 360f) % 360f,
            saturation = selectedColor.saturation.times(0.35f),
            value = 0.22f
        )
        val keyTextHSV: HSVColor = selectedColor.copy(
            saturation = (selectedColor.saturation * 0.1f).coerceIn(0f, 1f),
            value = 0.95f,
            alpha = 1.0f
        )
        val flickKeyBackgroundHSV: HSVColor = selectedColor.copy(
            hue = (((selectedColor.hue - 15f) % 360f) + 360f) % 360f,
            saturation = selectedColor.saturation.times(0.4f),
            value = 0.32f
        )
        val specialKeyBackgroundHSV: HSVColor = selectedColor.copy(
            saturation = (0.5f + selectedColor.saturation * 0.4f).coerceIn(0f, 1f),
            value = (0.45f + selectedColor.value * 0.25f).coerceIn(0f, 0.75f)
        )
        val specialKeyTextHSV: HSVColor = selectedColor.copy(
            saturation = 0.05f,
            value = 0.98f,
            alpha = 1.0f
        )

        return KeyboardColors(
            background = backgroundHSV.toColor(),
            keyBackground = keyBackgroundHSV.toColor(),
            keyText = keyTextHSV.toColor(),
            flickKeyBackground = flickKeyBackgroundHSV.toColor(),
            specialKeyBackground = specialKeyBackgroundHSV.toColor(),
            specialKeyText = specialKeyTextHSV.toColor()
        )
    }

    val backgroundHSV: HSVColor = selectedColor.copy(
        saturation = selectedColor.saturation.times(0.3f),
        value = (selectedColor.value * 1.5f - 0.5f).coerceIn(0f, 1f)
    )
    val keyBackgroundHSV: HSVColor = selectedColor.copy(
        hue = (((selectedColor.hue + 20f) % 360f) + 360f) % 360f,
        saturation = selectedColor.saturation.times(0.6f)
    )
    val keyTextHSV: HSVColor = selectedColor.copy(
        saturation = (selectedColor.value - selectedColor.saturation * 0.5f).coerceIn(0f, 1f),
        value = (1.5f - selectedColor.value * 1.5f).coerceIn(0f, 1f),
        alpha = 1.0f
    )
    val flickKeyBackgroundHSV: HSVColor = selectedColor.copy(
        hue = (((selectedColor.hue - 20f) % 360f) + 360f) % 360f,
        saturation = selectedColor.saturation.times(0.6f)
    )
    val specialKeyBackgroundHSV: HSVColor = selectedColor.copy(
        saturation = (1.0f - selectedColor.saturation * 0.5f).coerceIn(0f, 1f),
        value = (1.5f - selectedColor.value).coerceIn(0f, 1f)
    )
    val specialKeyTextHSV: HSVColor = selectedColor.copy(
        alpha = 1.0f
    )

    return KeyboardColors(
        background = backgroundHSV.toColor(),
        keyBackground = keyBackgroundHSV.toColor(),
        keyText = keyTextHSV.toColor(),
        flickKeyBackground = flickKeyBackgroundHSV.toColor(),
        specialKeyBackground = specialKeyBackgroundHSV.toColor(),
        specialKeyText = specialKeyTextHSV.toColor()
    )
}