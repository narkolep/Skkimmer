package com.narkolep.skkimmer.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.narkolep.skkimmer.keyboard.ui.theme.HSVColor
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 色相環(Hue-Saturation)のビットマップをキャッシュして生成する。
 * value(明度)は1.0固定で描画し、明度は別のスライダーで扱う。
 * IntArrayへ一括書き込みしてからBitmap化することで、
 * drawPointを1ピクセルずつ呼ぶより大幅に高速。sizePxが変わらない限り再計算しない。
 */
@Composable
private fun rememberColorWheelBitmap(sizePx: Int): ImageBitmap {
    return remember(sizePx) {
        val pixels = IntArray(sizePx * sizePx)
        val radius = sizePx / 2f
        val hsv = floatArrayOf(0f, 0f, 1f)

        for (y in 0 until sizePx) {
            val dy = y - radius
            for (x in 0 until sizePx) {
                val dx = x - radius
                val dist = sqrt(dx * dx + dy * dy)
                val index = y * sizePx + x
                if (dist <= radius) {
                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    if (angle < 0) angle += 360f
                    hsv[0] = angle
                    hsv[1] = (dist / radius).coerceIn(0f, 1f)
                    pixels[index] = AndroidColor.HSVToColor(hsv)
                } else {
                    pixels[index] = AndroidColor.TRANSPARENT
                }
            }
        }

        Bitmap.createBitmap(pixels, sizePx, sizePx, Bitmap.Config.ARGB_8888).asImageBitmap()
    }
}

/**
 *  hue(0..360) / saturation(0..1) から、ホイール中心を原点とした相対座標を求める。
 */
private fun polarToOffset(hue: Float, saturation: Float, radius: Float): Offset {
    val angleRad = Math.toRadians(hue.toDouble())
    val dist = saturation.coerceIn(0f, 1f) * radius
    return Offset(
        x = (cos(angleRad) * dist).toFloat(),
        y = (sin(angleRad) * dist).toFloat()
    )
}

/**
 * タップ/ドラッグ位置(Canvas内のローカル座標)を hue/saturation に変換する。
 * 中心からの距離が半径を超える場合は円周上にクランプするため、
 * ホイール外にドラッグしても選択位置が破綻しない。
 */
private fun offsetToHsv(
    position: Offset,
    canvasSizePx: Float,
    currentValue: Float,
    currentAlpha: Float
): HSVColor {
    val radius = canvasSizePx / 2f
    val dx = position.x - radius
    val dy = position.y - radius
    val dist = sqrt(dx * dx + dy * dy).coerceAtMost(radius)

    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    if (angle < 0) angle += 360f

    return HSVColor(
        hue = angle,
        saturation = (dist / radius).coerceIn(0f, 1f),
        value = currentValue,
        alpha = currentAlpha
    )
}

/**
 * 横方向のタップ/ドラッグ位置を 0..1 の割合に変換して通知する Modifier。
 * 明度スライダーとアルファスライダーで共通利用する。
 */
private fun Modifier.horizontalFractionInput(onFractionChanged: (Float) -> Unit): Modifier =
    this
        .pointerInput(Unit) {
            detectDragGestures { change, _ ->
                change.consume()
                onFractionChanged((change.position.x / this.size.width).coerceIn(0f, 1f))
            }
        }
        .pointerInput(Unit) {
            detectTapGestures { tapOffset ->
                onFractionChanged((tapOffset.x / this.size.width).coerceIn(0f, 1f))
            }
        }

/**
 * HSV色相環ベースのカラーピッカー。
 *
 * - 円形のホイールで hue(角度) と saturation(中心からの距離) を選択
 * - 下部のスライダーで value(明度) を選択
 * - その下のスライダーで alpha(不透明度) を選択(showAlpha = false で非表示)
 *
 * 状態は呼び出し側で保持する hoisting パターン(value / onValueChanged)。
 *
 * @param value 現在選択されている色
 * @param onValueChanged 色が変化したときに呼ばれるコールバック
 * @param wheelSize ホイールの直径
 * @param showAlpha アルファスライダーを表示するか
 */
@Composable
fun HarmonyColorPicker(
    value: HSVColor,
    onValueChanged: (HSVColor) -> Unit,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    wheelSize: Dp = 280.dp,
    showAlpha: Boolean = true
) {
    val currentColor by rememberUpdatedState(value)
    val onColorChanged by rememberUpdatedState(onValueChanged)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ColorWheel(
            value = currentColor,
            onValueChanged = onColorChanged,
            size = wheelSize
        )

        Spacer(modifier = Modifier.height(16.dp))

        BrightnessSlider(
            hue = currentColor.hue,
            saturation = currentColor.saturation,
            value = currentColor.value,
            onValueChanged = { newValue ->
                onColorChanged(currentColor.copy(value = newValue))
            },
            modifier = Modifier
                .width(wheelSize)
                .height(32.dp)
        )

        if (showAlpha) {
            Spacer(modifier = Modifier.height(12.dp))

            AlphaSlider(
                hue = currentColor.hue,
                saturation = currentColor.saturation,
                value = currentColor.value,
                alpha = currentColor.alpha,
                onAlphaChanged = { newAlpha ->
                    onColorChanged(currentColor.copy(alpha = newAlpha))
                },
                modifier = Modifier
                    .width(wheelSize)
                    .height(32.dp)
            )
        }
    }
}

@Composable
private fun ColorWheel(
    value: HSVColor,
    onValueChanged: (HSVColor) -> Unit,
    size: Dp
) {
    val density = LocalDensity.current
    val sizePx = remember(size, density) { with(density) { size.roundToPx() } }
    val wheelBitmap = rememberColorWheelBitmap(sizePx)

    val radiusPx = sizePx / 2f
    val magnifierRadiusPx = with(density) { 12.dp.toPx() }
    val magnifierBorderPx = with(density) { 2.dp.toPx() }

    Canvas(
        modifier = Modifier
            .size(size)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val updated = offsetToHsv(
                        position = change.position,
                        canvasSizePx = this.size.width.toFloat(),
                        currentValue = value.value,
                        currentAlpha = value.alpha
                    )
                    onValueChanged(updated)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val updated = offsetToHsv(
                        position = tapOffset,
                        canvasSizePx = this.size.width.toFloat(),
                        currentValue = value.value,
                        currentAlpha = value.alpha
                    )
                    onValueChanged(updated)
                }
            }
    ) {
        drawImage(wheelBitmap)

        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        // 選択中の色のマグニファイア
        // alphaを下げてもマーカー自体が透けて見えなくならないよう、alpha = 1f で描画する
        val selectedPos = center + polarToOffset(value.hue, value.saturation, radiusPx)
        drawCircle(
            color = value.copy(value = 1f, alpha = 1f).toColor(),
            radius = magnifierRadiusPx,
            center = selectedPos
        )
        drawCircle(
            color = Color.White,
            radius = magnifierRadiusPx,
            center = selectedPos,
            style = Stroke(width = magnifierBorderPx)
        )
    }
}

/**
 * 明度(value)を選択するスライダー。
 * 現在の hue/saturation に対して、明度0→1のグラデーションバーを描画する。
 */
@Composable
private fun BrightnessSlider(
    hue: Float,
    saturation: Float,
    value: Float,
    onValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val fullBrightColor = HSVColor(hue, saturation, 1f).toColor()

    Canvas(
        modifier = modifier.horizontalFractionInput(onValueChanged)
    ) {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Black, fullBrightColor)
            )
        )

        val knobX = value.coerceIn(0f, 1f) * size.width
        drawCircle(
            color = Color.White,
            radius = size.height / 2f + 2.dp.toPx(),
            center = Offset(knobX, size.height / 2f)
        )
        drawCircle(
            color = HSVColor(hue, saturation, value).toColor(),
            radius = size.height / 2f - 2.dp.toPx(),
            center = Offset(knobX, size.height / 2f)
        )
    }
}

/**
 * 不透明度(alpha)を選択するスライダー。
 * 透明度が分かるよう、背景に市松模様を敷き、その上に
 * 現在の色の「透明 → 不透明」グラデーションを重ねて描画する。
 */
@Composable
private fun AlphaSlider(
    hue: Float,
    saturation: Float,
    value: Float,
    alpha: Float,
    onAlphaChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // 市松模様の色は固定値なので、毎回確保しないよう remember しておく
    val checkerLight = remember { Color(0xFFFFFFFF) }
    val checkerDark = remember { Color(0xFFCCCCCC) }
    val baseColor = HSVColor(hue, saturation, value).toColor().copy(alpha = 1f)

    Canvas(
        modifier = modifier.horizontalFractionInput(onAlphaChanged)
    ) {
        // 1. 市松模様(透明部分が分かるようにする背景)
        val cell = size.height / 4f
        val cols = kotlin.math.ceil(size.width / cell).toInt()
        val rows = kotlin.math.ceil(size.height / cell).toInt()
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val topLeft = Offset(col * cell, row * cell)
                drawRect(
                    color = if ((row + col) % 2 == 0) checkerLight else checkerDark,
                    topLeft = topLeft,
                    // 端のセルがはみ出さないように切り詰める
                    size = Size(
                        width = min(cell, size.width - topLeft.x),
                        height = min(cell, size.height - topLeft.y)
                    )
                )
            }
        }

        // 2. 透明 → 不透明のグラデーション
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(baseColor.copy(alpha = 0f), baseColor)
            )
        )

        // 3. ノブ(外側は白リング、内側は現在のalpha付きの色)
        val knobX = alpha.coerceIn(0f, 1f) * size.width
        drawCircle(
            color = Color.White,
            radius = size.height / 2f + 2.dp.toPx(),
            center = Offset(knobX, size.height / 2f)
        )
        drawCircle(
            color = baseColor.copy(alpha = alpha.coerceIn(0f, 1f)),
            radius = size.height / 2f - 2.dp.toPx(),
            center = Offset(knobX, size.height / 2f)
        )
    }
}