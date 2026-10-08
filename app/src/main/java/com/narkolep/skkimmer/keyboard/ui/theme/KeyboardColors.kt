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

fun keyboardColors(selectedColor: HSVColor): KeyboardColors {
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
        value = (1.5f - selectedColor.value * 1.5f).coerceIn(0f, 1f)
    )
    val flickKeyBackgroundHSV: HSVColor = selectedColor.copy(
        hue = (((selectedColor.hue - 20f) % 360f) + 360f) % 360f,
        saturation = selectedColor.saturation.times(0.6f)
    )
    val specialKeyBackgroundHSV: HSVColor = selectedColor.copy(
        saturation = (1.0f - selectedColor.saturation * 0.5f).coerceIn(0f, 1f),
        value = (1.5f - selectedColor.value).coerceIn(0f, 1f)
    )
    val specialKeyTextHSV: HSVColor = selectedColor

    return KeyboardColors(
        background = backgroundHSV.toColor(),
        keyBackground = keyBackgroundHSV.toColor(),
        keyText = keyTextHSV.toColor(),
        flickKeyBackground = flickKeyBackgroundHSV.toColor(),
        specialKeyBackground = specialKeyBackgroundHSV.toColor(),
        specialKeyText = specialKeyTextHSV.toColor()
    )
}