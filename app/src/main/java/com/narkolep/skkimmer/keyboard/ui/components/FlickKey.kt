package com.narkolep.skkimmer.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.abs
import androidx.compose.ui.res.painterResource
import com.narkolep.skkimmer.keyboard.ui.layouts.FlickKeyConfig
import com.narkolep.skkimmer.keyboard.ui.layouts.Position

enum class FlickDirection {
    NONE, CENTER, LEFT, RIGHT, UP, DOWN
}

@Composable
fun FlickKey(
    modifier: Modifier = Modifier,
    config: FlickKeyConfig,
    displayText: Position = Position(),
    keyColor: Color,
    textColor: Color,
    iconResId: Int? = null,
    isNumberKeyboard: Boolean = false,
    onInput: (String) -> Unit,
    onFeedback: () -> Unit,
) {
    var currentDir by remember { mutableStateOf(FlickDirection.NONE) }
    var isDragging by remember { mutableStateOf(false) }
    val threshold = 40f

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(3.dp)
            .fillMaxHeight()
            .background(
                color = keyColor,
                shape = RoundedCornerShape(10.dp)
            )
            .pointerInput(config, displayText) {
                awaitEachGesture {
                    val downEvent = awaitFirstDown(requireUnconsumed = false)

                    onFeedback()

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

                            /* 方向判定 */
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
                modifier = Modifier.size((maxHeight.value * 0.5f).dp),
                tint = textColor
            )
        } else {
            if (isNumberKeyboard) {
                Text(
                    text = config.flickPosition.left + config.flickPosition.up + config.flickPosition.down + config.flickPosition.right,
                    fontSize = (maxHeight.value * 0.2f).sp,
                    color = textColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(2.dp)
                )
            }

            Text(
                text = displayText.center.ifEmpty { config.label },
                color = textColor,
                fontSize = (maxHeight.value * 0.5f).sp,
                modifier = Modifier.offset(y = if (isNumberKeyboard) (-4).dp else 0.dp)
            )
        }

        /* ドラッグ中のみポップアップを表示 */
        if (isDragging) {
            Popup(
                alignment = Alignment.Center,
                offset = with(LocalDensity.current) {
                    IntOffset(
                        x = 0,
                        y = (maxHeight.value * -1.5f).dp.roundToPx()
                    )
                }
            ) {
                FlickPopup(
                    flickText = if (displayText == Position()) config.flickPosition else displayText,
                    currentDir = currentDir
                )
            }
        }
    }
}