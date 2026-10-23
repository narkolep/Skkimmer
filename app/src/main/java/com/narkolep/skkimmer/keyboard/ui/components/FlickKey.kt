package com.narkolep.skkimmer.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.narkolep.skkimmer.keyboard.ui.layouts.FlickKanaMap
import kotlin.math.abs
import androidx.compose.ui.res.painterResource

enum class FlickDirection {
    NONE, CENTER, LEFT, RIGHT, UP, DOWN
}

@Composable
fun FlickKey(
    modifier: Modifier = Modifier,
    config: FlickKanaMap.FlickKeyConfig,
    displayText: FlickKanaMap.Position = FlickKanaMap.Position(),
    keyColor: Color,
    textColor: Color,
    iconResId: Int? = null,
    isNumberKeyboard: Boolean = false, // テンキーの場合、記号を表示する
    onInput: (String) -> Unit
) {
    var currentDir by remember { mutableStateOf(FlickDirection.NONE) }
    var isDragging by remember { mutableStateOf(false) }
    val threshold = 40f

    val density = LocalDensity.current
    var keyWidth by remember { mutableStateOf(0.dp) }
    var keyHeight by remember { mutableStateOf(0.dp) }

    var keyHeightPx by remember { mutableIntStateOf(0) }
    var keyWidthPx by remember { mutableIntStateOf(0) }

    var yOffset by remember { mutableIntStateOf(0) }
    var xOffset by remember { mutableIntStateOf(0) }

    val paddingDp = 3.dp
    val paddingPx = with(density) { paddingDp.roundToPx() }
    val offsetDp: Dp = if (isNumberKeyboard) (-4).dp else 0.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(paddingDp)
            .fillMaxHeight()
            .background(color = keyColor, shape = RoundedCornerShape(10.dp))
            .onSizeChanged { size ->
                keyWidth = with(density) { size.width.toDp() }
                keyHeight = with(density) { size.height.toDp() }
                keyWidthPx = size.width
                keyHeightPx = size.height

                yOffset = 0
                xOffset = 0
                if (config.flickPosition.up.isEmpty()) yOffset += (keyHeightPx*0.5 + paddingPx*0.5).toInt()
                if (config.flickPosition.down.isEmpty()) yOffset -= (keyHeightPx*0.5 + paddingPx*0.5).toInt()
                if (config.flickPosition.left.isEmpty()) xOffset += (keyWidthPx*0.5 + paddingPx).toInt()
                if (config.flickPosition.right.isEmpty()) xOffset -= (keyWidthPx*0.5 + paddingPx).toInt()
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val downEvent = awaitFirstDown()

                    isDragging = true
                    currentDir = FlickDirection.CENTER

                    var finalX: Float
                    var finalY: Float

                    do {
                        val event = awaitPointerEvent()
                        val pointer = event.changes.firstOrNull()
                        if (pointer != null) {
                            val dragOffset = pointer.position - downEvent.position
                            finalX = dragOffset.x
                            finalY = dragOffset.y

                            // 方向判定
                            currentDir = when {
                                abs(finalX) < threshold && abs(finalY) < threshold -> FlickDirection.CENTER
                                abs(finalX) > abs(finalY) -> if (finalX > 0) FlickDirection.RIGHT else FlickDirection.LEFT
                                else -> if (finalY > 0) FlickDirection.DOWN else FlickDirection.UP
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    isDragging = false
                    val output = when(currentDir) {
                        FlickDirection.LEFT -> config.flickPosition.left
                        FlickDirection.RIGHT -> config.flickPosition.right
                        FlickDirection.UP -> config.flickPosition.up
                        FlickDirection.DOWN -> config.flickPosition.down
                        else -> config.flickPosition.center
                    }
                    currentDir = FlickDirection.NONE

                    if (output.isNotEmpty()) onInput(config.consonant + output)
                }
            }
    ) {
        if (iconResId != null) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = config.label,
                modifier = Modifier.size(24.dp),
                tint = textColor
            )
        } else {
            if (isNumberKeyboard) {
                Text(
                    text = config.flickPosition.left + config.flickPosition.up + config.flickPosition.down + config.flickPosition.right,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(2.dp)
                )
            }

            Text(
                text = if (displayText == FlickKanaMap.Position()) config.label else displayText.center,
                color = textColor,
                fontSize = 26.sp,
                modifier = Modifier.offset(y = offsetDp)
            )
        }

        // ドラッグ中のみポップアップを表示
        if (isDragging) {
            Popup(
                alignment = Alignment.Center,
                offset = IntOffset(x = xOffset, y = yOffset)
            ) {
                FlickPopup(
                    flickText = if (displayText == FlickKanaMap.Position()) config.flickPosition else displayText,
                    currentDir = currentDir,
                    paddingDp = paddingDp,
                    keyWidth = keyWidth + paddingDp,
                    keyHeight = keyHeight + paddingDp
                )
            }
        }
    }
}