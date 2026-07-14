package com.narkolep.skkimmer.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.narkolep.skkimmer.ui.theme.AppFontFamily
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SkkKey(
    mainText: String, // 表示する文字
    modifier: Modifier = Modifier,
    iconResId: Int? = null, // アイコン(svg)
    flickText: String = "",
    textSize: Float = 24f,
    keyboardHeight: Float,
    keyColor: Color,
    textColor: Color,
    flickColor: Color = Color.Transparent,
    spaceLeftRight: String = "",
    weight: Float = 1f,
    cornerShape: Dp = 8.dp,
    keyRepeat: Boolean = false,
    onFlick: () -> Unit = {},
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var currentPopupText by remember { mutableStateOf(mainText) }

    val density = LocalDensity.current
    val flickHeightPx = with(density) { (keyboardHeight*0.8).dp.roundToPx() }

    var weight = weight

    // バックグラウンドでタイマーを動かすためのスコープ
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(mainText, flickText) {
                val flickThreshold = 40.dp.toPx()
                val repeatInterval = 100L // 連続入力の間隔 (100ms)
                val initialDelay = 500L   // 長押しと判定するまでの待機時間 (500ms)

                awaitEachGesture {
                    val downEvent = awaitFirstDown(requireUnconsumed = false)

                    downEvent.consume()
                    isPressed = true

                    var repeatJob: Job? = null // タイマーを管理する変数

                    try {
                        if (flickText.isEmpty()) {
                            // 押した瞬間に確定（1回目の入力）
                            onClick()

                            // リピート開始
                            if (keyRepeat) {
                                repeatJob = scope.launch {
                                    delay(initialDelay.milliseconds) // 最初は500ms待つ
                                    while (isActive) {
                                        onClick() // アクションを実行
                                        delay(repeatInterval.milliseconds) // 100ms待ってからループ
                                    }
                                }
                            }

                            // 確定させた後も、指が画面から完全に離れるまでは空回りさせて待機する
                            do {
                                val event = awaitPointerEvent()
                                event.changes.forEach { it.consume() }
                            } while (event.changes.any { it.pressed })
                        } else {
                            currentPopupText = mainText
                            var isFlick = false

                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull()

                                if (change != null) {
                                    change.consume()
                                    val distance = (change.position - downEvent.position).getDistance()

                                    if (distance > flickThreshold) {
                                        isFlick = true
                                        currentPopupText = flickText
                                    } else {
                                        isFlick = false
                                        currentPopupText = mainText
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            if (isFlick) {
                                onFlick()
                            } else {
                                onClick()
                            }
                        }
                    } finally {
                        // 指が離れたらタイマー停止
                        repeatJob?.cancel()

                        // 指が離れたら色を戻す
                        isPressed = false
                    }
                }
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (spaceLeftRight.isNotEmpty()) weight -= 0.5f
            if (spaceLeftRight == "Left") {
                Spacer(modifier = Modifier.weight(0.5f))
            }

            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .weight(weight)
                    .fillMaxHeight()
                    .background(
                        color = if (isPressed) keyColor.copy(alpha = 0.6f) else keyColor,
                        shape = RoundedCornerShape(cornerShape)
                    )
            ) {
                if (iconResId != null) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = mainText,
                        modifier = Modifier
                            .size(26.dp)
                            .align(Alignment.Center),
                        tint = textColor
                    )
                } else {
                    if (flickText.isNotEmpty()) {
                        Text(
                            text = flickText,
                            fontSize = 10.sp,
                            fontFamily = AppFontFamily,
                            color = textColor.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                        )
                    }

                    Text(
                        text = mainText,
                        fontWeight = if (flickText.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = textSize.sp,
                        fontFamily = AppFontFamily,
                        color = textColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // ポップアップの表示
                if (isPressed && flickText.isNotEmpty()) {
                    Popup(
                        alignment = Alignment.Center,
                        offset = IntOffset(x = 0, y = -flickHeightPx)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = (keyboardHeight * 0.9).dp)
                                .background(flickColor, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentPopupText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 28.sp,
                                fontFamily = AppFontFamily,
                                color = textColor,
                            )
                        }
                    }
                }
            }

            if (spaceLeftRight == "Right") {
                Spacer(modifier = Modifier.weight(0.5f))
            }
        }
    }
}