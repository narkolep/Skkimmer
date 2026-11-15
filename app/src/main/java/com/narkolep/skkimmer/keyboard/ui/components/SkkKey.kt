package com.narkolep.skkimmer.keyboard.ui.components

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.narkolep.skkimmer.HAPTIC_ENABLED_KEY
import com.narkolep.skkimmer.SOUND_ENABLED_KEY
import com.narkolep.skkimmer.dataStore
import kotlin.time.Duration.Companion.milliseconds

import com.narkolep.skkimmer.keyboard.ui.layouts.KeySpacerPosition
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun SkkKey(
    modifier: Modifier = Modifier,
    mainText: String,
    flickText: String = "",
    iconResId: Int? = null,

    keyColor: Color,
    textColor: Color,
    flickColor: Color = Color.Transparent,

    keyboardHeight: Float,
    spaceLeftRight: KeySpacerPosition = KeySpacerPosition.NONE,
    weight: Float = 1f,
    cornerShape: Dp = 10.dp,
    keyRepeat: Boolean = false,
    onFlick: () -> Unit = {},
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    val hapticEnabled by remember(context) {
        context.dataStore.data.map { prefs ->
            prefs[HAPTIC_ENABLED_KEY] ?: true
        }.distinctUntilChanged()
    }.collectAsState(initial = true)

    val soundEnabled by remember(context) {
        context.dataStore.data.map { prefs ->
            prefs[SOUND_ENABLED_KEY] ?: false
        }.distinctUntilChanged()
    }.collectAsState(initial = false)

    // フィードバック発火関数
    fun performFeedback() {
        if (hapticEnabled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
        }
        if (soundEnabled) {
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
    }

    var isPressed by remember { mutableStateOf(false) }
    var currentPopupText by remember { mutableStateOf(mainText) }

    val actualWeight = if (spaceLeftRight != KeySpacerPosition.NONE) weight - 0.5f else weight

    // バックグラウンドでタイマーを動かすためのスコープ
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(mainText, flickText) {
                val flickThreshold = 40.dp.toPx()
                val repeatInterval = 80L // 連続入力の間隔
                val initialDelay = 500L // 長押しと判定するまでの待機時間

                awaitEachGesture {
                    val downEvent = awaitFirstDown(requireUnconsumed = false)

                    performFeedback()

                    downEvent.consume()
                    isPressed = true

                    var repeatJob: Job? = null

                    try {
                        if (flickText.isEmpty()) {
                            // 押した瞬間に確定（1回目の入力）
                            onClick()

                            // リピート開始
                            if (keyRepeat) {
                                repeatJob = scope.launch {
                                    delay(initialDelay.milliseconds)
                                    // 触覚フィードバック
                                    if (hapticEnabled) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }

                                    while (isActive) {
                                        // アクションを実行
                                        onClick()
                                        delay(repeatInterval.milliseconds)
                                    }
                                }
                            }

                            // 指が画面から完全に離れるまで待機
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
            if (spaceLeftRight == KeySpacerPosition.LEFT) {
                Spacer(modifier = Modifier.weight(0.5f))
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(3.dp)
                    .weight(actualWeight)
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
                        modifier = Modifier.size(((keyboardHeight - 6f) * 0.5f).dp),
                        tint = textColor
                    )
                } else {
                    if (flickText.isNotEmpty()) {
                        Text(
                            text = flickText,
                            fontSize = (keyboardHeight * 0.18f).sp,
                            color = textColor.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                        )
                    }

                    Text(
                        text = mainText,
                        fontSize =
                            if (flickText.isNotEmpty()) (keyboardHeight * 0.45f).sp
                            else (keyboardHeight * 0.3f).sp,
                        color = textColor
                    )
                }

                // ポップアップの表示
                if (isPressed && flickText.isNotEmpty()) {
                    Popup(
                        alignment = Alignment.Center,
                        offset = with(LocalDensity.current) {
                            IntOffset(
                                x = 0,
                                y = (keyboardHeight * -1f).dp.roundToPx()
                            )
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 50.dp, height = keyboardHeight.dp)
                                .shadow(4.dp, RoundedCornerShape(10.dp))
                                .background(flickColor.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
                                .border(1.dp, textColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentPopupText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = (keyboardHeight * 0.55f).sp,
                                color = textColor
                            )
                        }
                    }
                }
            }

            if (spaceLeftRight == KeySpacerPosition.RIGHT) {
                Spacer(modifier = Modifier.weight(0.5f))
            }
        }
    }
}