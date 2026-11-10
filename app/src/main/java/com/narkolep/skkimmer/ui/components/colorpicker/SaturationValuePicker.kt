package com.narkolep.skkimmer.ui.components.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun SaturationValuePicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onValueChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(hue) {
                detectTapGestures { tapOffset ->
                    val s = (tapOffset.x / size.width).coerceIn(0f, 1f)
                    val v = (1f - tapOffset.y / size.height).coerceIn(0f, 1f)
                    onValueChange(s, v)
                }
            }
            .pointerInput(hue) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val s = (startOffset.x / size.width).coerceIn(0f, 1f)
                        val v = (1f - startOffset.y / size.height).coerceIn(0f, 1f)
                        onValueChange(s, v)
                    }
                ) { change, _ ->
                    val s = (change.position.x / size.width).coerceIn(0f, 1f)
                    val v = (1f - change.position.y / size.height).coerceIn(0f, 1f)
                    onValueChange(s, v)
                    change.consume()
                }
            }
    ) {
        // Hue
        drawRect(
            color = Color.hsv(hue, 1f, 1f)
        )

        // Saturation: White -> Transparent
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(Color.White, Color.Transparent)
            )
        )

        // Value: Transparent -> Black
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color.Transparent, Color.Black)
            )
        )

        // Selection indicator
        val x = saturation * size.width
        val y = (1f - value) * size.height

        drawCircle(
            color = Color.White,
            radius = 14.dp.toPx(),
            center = Offset(x, y),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}