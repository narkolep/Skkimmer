package com.narkolep.skkimmer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * スライダー付きのポップアップ
 */
@Composable
fun DpSliderItem(
    title: String,
    currentValue: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 5,
    onValueChanged: (Float) -> Unit
) {
    /* ポップアップの表示状態を管理する変数 */
    var showDialog by remember { mutableStateOf(false) }

    /* 設定画面に表示されるリスト項目 */
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text("${currentValue.roundToInt()} dp") },
        modifier = Modifier.clickable { showDialog = true }
    )

    /* ポップアップ（ダイアログ）の中身 */
    if (showDialog) {
        /* スライダー操作中の一時的な値を保持 */
        var sliderPosition by remember(currentValue) { mutableFloatStateOf(currentValue) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    /* 現在のスライダーの値を表示 */
                    Text(
                        text = "${sliderPosition.roundToInt()} dp",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    /* スライダー本体 */
                    Slider(
                        value = sliderPosition,
                        onValueChange = { newValue ->
                            sliderPosition = newValue
                        },
                        valueRange = valueRange,
                        steps = steps
                    )
                }
            },
            confirmButton = {
                // OKボタン
                TextButton(onClick = {
                    onValueChanged(sliderPosition) // ここで保存処理を実行
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                // キャンセルボタン
                TextButton(onClick = {
                    showDialog = false // 保存せずにただ閉じる
                }) {
                    Text("キャンセル")
                }
            }
        )
    }
}