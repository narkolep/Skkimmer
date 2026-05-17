package com.narkolep.skkimmer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * SETTING SCREEN内のアイテム
 **/

// 横線
@Composable
fun Divider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    )
}

// 見出し
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, bottom = 8.dp, end = 16.dp)
    )
}

// 設定項目 (他画面に遷移する、等)
@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        modifier = Modifier.clickable { onClick() }
    )
}

// 確認ダイアログ
@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {Text(text)},
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

// ラジオボタン付きのポップアップ
@Composable
fun ThemeSettingItem(
    selected: String,
    onThemeSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val themes = listOf("ライト", "ダーク", "システム")

    ListItem(
        headlineContent = { Text("テーマの選択") },
        supportingContent = { Text(selected) },
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        var tempSelected by remember { mutableStateOf(selected) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("テーマの選択") },
            text = {
                Column {
                    themes.forEach { theme ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // タップした時は一時的な状態を更新するだけ（保存はしない）
                                    tempSelected = theme
                                }
                                .padding(vertical = 0.dp)
                        ) {
                            RadioButton(
                                selected = (theme == tempSelected),
                                onClick = { tempSelected = theme }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = theme)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onThemeSelected(tempSelected)
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

// スライダー付きのポップアップ
@Composable
fun KeyboardHeightSliderItem(
    title: String,
    currentHeight: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onHeightChanged: (Float) -> Unit
) {
    // --- ポップアップの表示状態を管理する変数 ---
    var showDialog by remember { mutableStateOf(false) }

    // --- 設定画面に表示されるリスト項目 ---
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text("${currentHeight.roundToInt()} dp") },
        modifier = Modifier.clickable { showDialog = true }
    )

    // --- ポップアップ（ダイアログ）の中身 ---
    if (showDialog) {
        // スライダー操作中の一時的な値を保持
        var sliderPosition by remember(currentHeight) { mutableStateOf(currentHeight) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 現在のスライダーの値を表示
                    Text(
                        text = "${sliderPosition.roundToInt()} dp",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // スライダー本体
                    Slider(
                        value = sliderPosition,
                        onValueChange = { newValue ->
                            sliderPosition = newValue
                        },
                        valueRange = valueRange,
                        steps = 5
                    )
                }
            },
            confirmButton = {
                // OKボタン
                TextButton(onClick = {
                    onHeightChanged(sliderPosition) // ここで保存処理を実行
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