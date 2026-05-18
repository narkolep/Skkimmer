package com.narkolep.skkimmer.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Popup
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.narkolep.skkimmer.keyboard.mappings.FlickKanaMap
import kotlin.math.abs
import androidx.compose.ui.res.painterResource

enum class FlickDirection {
    NONE, CENTER, LEFT, RIGHT, UP, DOWN
}

@Composable
fun FlickKey(
    config: FlickKanaMap.FlickKeyConfig,
    modifier: Modifier = Modifier,
    displayText: String = "",
    keyColor: Color = Color.Black,
    textColor: Color = Color.Gray,
    backgroundColor: Color = Color.LightGray,
    actionColor: Color = Color.DarkGray,
    iconResId: Int? = null,
    isFlickMode: Boolean = true,
    isCtrlPressed: Boolean = false,
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

    val paddingPx = with(density) { 4.dp.roundToPx() }

    val currentIsCtrlActive by rememberUpdatedState(isCtrlPressed)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(2.dp)
            .fillMaxHeight()
            .background(keyColor, shape = RoundedCornerShape(8.dp))
            .onSizeChanged { size ->
                keyWidth = with(density) { size.width.toDp() }
                keyHeight = with(density) { size.height.toDp() }
                keyWidthPx = size.width
                keyHeightPx = size.height

                yOffset = 0
                xOffset = 0
                if (config.up.isEmpty()) yOffset += ((keyHeightPx + paddingPx)*0.5).toInt()
                if (config.down.isEmpty()) yOffset -= ((keyHeightPx + paddingPx)*0.5).toInt()
                if (config.left.isEmpty()) xOffset += ((keyWidthPx + paddingPx)*0.5).toInt()
                if (config.right.isEmpty()) xOffset -= ((keyWidthPx + paddingPx)*0.5).toInt()
            }
            .pointerInput(isFlickMode) {
                if (!isFlickMode) return@pointerInput

                awaitEachGesture {
                    val downEvent = awaitFirstDown()
                    if (config.consonant.isNotEmpty()) onInput(config.consonant)

                    isDragging = true // 指が触れたら表示
                    currentDir = FlickDirection.CENTER

                    var finalX = 0f
                    var finalY = 0f

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

                    isDragging = false // 指が離れたら非表示
                    val output = when(currentDir) {
                        FlickDirection.CENTER -> config.center
                        FlickDirection.LEFT -> config.left
                        FlickDirection.RIGHT -> config.right
                        FlickDirection.UP -> config.up
                        FlickDirection.DOWN -> config.down
                        else -> config.center
                    }
                    currentDir = FlickDirection.NONE

                    val shouldSkipVowel = currentIsCtrlActive && config.consonant.isNotEmpty()
                    if (!shouldSkipVowel && output.isNotEmpty()) onInput(output)
                }
            }
    ) {
        if (iconResId != null) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = displayText,
                modifier = Modifier.size(24.dp),
                tint = textColor
            )
        } else {
            Text(
                text = displayText,
                color = textColor,
                fontSize = 24.sp
            )
        }

        // ドラッグ中のみポップアップを表示
        if (isDragging) {
            Popup(
                alignment = Alignment.Center,
                offset = IntOffset(x = xOffset, y = yOffset)
            ) {
                FlickPopup(
                    config = config,
                    currentDir = currentDir,
                    keyWidth = keyWidth + 2.dp,
                    keyHeight = keyHeight + 2.dp,
                    activeBgColor = keyColor,
                    activeTextColor = actionColor,
                    inactiveBgColor = backgroundColor,
                    inactiveTextColor = textColor
                )
            }
        }
    }
}

@Composable
fun FlickPopup(
    config: FlickKanaMap.FlickKeyConfig,
    currentDir: FlickDirection,
    keyWidth: Dp,
    keyHeight: Dp,
    activeBgColor: Color = Color.Blue,
    activeTextColor: Color = Color.White,
    inactiveBgColor: Color = Color.LightGray,
    inactiveTextColor: Color = Color.DarkGray
) {
    // 1つの文字ブロックを描画する関数
    @Composable
    fun PopupChar(text: String, isActive: Boolean) {
        if (text.isEmpty()) {
            Spacer(modifier = Modifier.size(width = keyWidth, height = keyHeight))
            return
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = keyWidth, height = keyHeight)
                .background(
                    color = if (isActive) activeBgColor else inactiveBgColor,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Text(
                text = text,
                color = if (isActive) activeTextColor else inactiveTextColor,
                fontSize = 24.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }

    // レイアウト
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.padding(2.dp)
    ) {
        // 上段
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (config.up.isNotEmpty()) {
                if (config.left.isNotEmpty()) PopupChar("", false)
                PopupChar(text = config.up, isActive = currentDir == FlickDirection.UP)
                if (config.right.isNotEmpty()) PopupChar("", false)
            }
        }
        // 中段
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (config.left.isNotEmpty()) PopupChar(text = config.left, isActive = currentDir == FlickDirection.LEFT)
            PopupChar(text = config.center, isActive = currentDir == FlickDirection.CENTER)
            if (config.right.isNotEmpty()) PopupChar(text = config.right, isActive = currentDir == FlickDirection.RIGHT)
        }
        // 下段
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (config.down.isNotEmpty()) {
                if (config.left.isNotEmpty()) PopupChar("", false)
                PopupChar(text = config.down, isActive = currentDir == FlickDirection.DOWN)
                if (config.right.isNotEmpty()) PopupChar("", false)
            }
        }
    }
}