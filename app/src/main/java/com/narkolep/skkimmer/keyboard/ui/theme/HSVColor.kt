package com.narkolep.skkimmer.keyboard.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

/**
 * HSV色を表すデータクラス。
 * hue: 0f..360f
 * saturation/value/alpha: 0f..1f
 */
data class HSVColor(
    val hue: Float,
    val saturation: Float,
    val value: Float,
    val alpha: Float = 1f
) {
    fun toColor(): Color {
        val argb = AndroidColor.HSVToColor(
            (alpha * 255).roundToInt().coerceIn(0, 255),
            floatArrayOf(hue, saturation, value)
        )
        return Color(argb)
    }

    companion object {
        val Default = HSVColor(hue = 210f, saturation = 0.6f, value = 0.9f)
    }
}