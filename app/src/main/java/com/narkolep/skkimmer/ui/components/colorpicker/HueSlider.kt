package com.narkolep.skkimmer.ui.components.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
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
fun HueSlider(
    hue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color.Red,
        Color.Yellow,
        Color.Green,
        Color.Cyan,
        Color.Blue,
        Color.Magenta,
        Color.Red
    )

    Canvas(
        modifier = modifier
            .height(32.dp)
            .pointerInput(Unit) {
                fun update(position: Offset) {
                    onHueChange(
                        (position.x / size.width * 360f)
                            .coerceIn(0f, 360f)
                    )
                }

                detectTapGestures { update(it) }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { position ->
                        onHueChange(
                            (position.x / size.width * 360f)
                                .coerceIn(0f, 360f)
                        )
                    }
                ) { change, _ ->
                    onHueChange(
                        (change.position.x / size.width * 360f)
                            .coerceIn(0f, 360f)
                    )
                    change.consume()
                }
            }
    ) {
        drawRect(
            brush = Brush.horizontalGradient(colors)
        )

        val x = hue / 360f * size.width

        drawCircle(
            color = Color.White,
            radius = 13.dp.toPx(),
            center = Offset(x, size.height / 2),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}