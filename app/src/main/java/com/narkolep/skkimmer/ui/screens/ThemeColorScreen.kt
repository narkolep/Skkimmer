package com.narkolep.skkimmer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.narkolep.skkimmer.ALPHA_KEY
import com.narkolep.skkimmer.BLUR_ENABLED_KEY
import com.narkolep.skkimmer.BLUR_RADIUS_KEY
import com.narkolep.skkimmer.DARK_MODE_KEY
import com.narkolep.skkimmer.HUE_KEY
import com.narkolep.skkimmer.SATURATION_KEY
import com.narkolep.skkimmer.THEME_SELECT_KEY
import com.narkolep.skkimmer.VALUE_KEY
import com.narkolep.skkimmer.dataStore
import com.narkolep.skkimmer.keyboard.KeyboardService
import com.narkolep.skkimmer.keyboard.ui.theme.HSVColor
import com.narkolep.skkimmer.keyboard.ui.theme.LocalKeyboardColors
import com.narkolep.skkimmer.ui.components.ColorPreviewCard
import com.narkolep.skkimmer.ui.components.Divider
import com.narkolep.skkimmer.ui.components.InlineSliderItem
import com.narkolep.skkimmer.ui.components.SectionHeader
import com.narkolep.skkimmer.ui.components.SwitchSettingItem
import com.narkolep.skkimmer.ui.components.colorpicker.ColorPickerDialog
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeColorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // DataStoreから設定を読み取る
    val isCustomTheme by remember(context) {
        context.dataStore.data.map { preferences ->
            preferences[THEME_SELECT_KEY] ?: false
        }.distinctUntilChanged()
    }.collectAsState(initial = false)

    val useDarkMode by remember(context) {
        context.dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }.distinctUntilChanged()
    }.collectAsState(initial = false)

    val isBlurEnabled by remember(context) {
        context.dataStore.data.map { preferences ->
            preferences[BLUR_ENABLED_KEY] ?: false
        }.distinctUntilChanged()
    }.collectAsState(initial = false)

    val blurRadius by remember(context) {
        context.dataStore.data.map { preferences ->
            preferences[BLUR_RADIUS_KEY] ?: KeyboardService.DEFAULT_BLUR_RADIUS_PX
        }.distinctUntilChanged()
    }.collectAsState(initial = KeyboardService.DEFAULT_BLUR_RADIUS_PX)

    val currentHsv by remember(context) {
        context.dataStore.data.map { preferences ->
            HSVColor(
                hue = preferences[HUE_KEY] ?: 0f,
                saturation = preferences[SATURATION_KEY] ?: 0f,
                value = preferences[VALUE_KEY] ?: 0f,
                alpha = preferences[ALPHA_KEY] ?: 1f,
            )
        }.distinctUntilChanged()
    }.collectAsState(initial = HSVColor.Default)

    var showColorPickerDialog by remember { mutableStateOf(false) }

    // スライダーのリアルタイム操作用ローカル状態
    var localAlpha by remember(currentHsv.alpha) { mutableFloatStateOf(currentHsv.alpha) }
    var localBlurRadius by remember(blurRadius) { mutableFloatStateOf(blurRadius.toFloat()) }

    val colors = LocalKeyboardColors.current

    if (showColorPickerDialog) {
        ColorPickerDialog(
            currentHsv = currentHsv,
            defaultHsv = HSVColor.Default,
            onDismiss = { showColorPickerDialog = false },
            onSelect = { selectedHsv ->
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[HUE_KEY] = selectedHsv.hue
                        preferences[SATURATION_KEY] = selectedHsv.saturation
                        preferences[VALUE_KEY] = selectedHsv.value
                        preferences[ALPHA_KEY] = selectedHsv.alpha
                        preferences[THEME_SELECT_KEY] = true
                    }
                }
                showColorPickerDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("テーマカラー") })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {

            item { Divider() }
            item { SectionHeader("プレビュー") }
            item {
                ColorPreviewCard(
                    colors = colors,
                    isBlurEnabled = isBlurEnabled,
                    alpha = localAlpha
                )
            }

            item { Divider() }
            item { SectionHeader("テーマ設定") }
            item {
                SwitchSettingItem(
                    title = "カスタムテーマを使用",
                    subtitle = "有効化するとテーマを変更することができます",
                    checked = isCustomTheme,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            context.dataStore.edit { preferences ->
                                preferences[THEME_SELECT_KEY] = enabled
                            }
                        }
                    }
                )
            }

            if (isCustomTheme) {
                item {
                    SwitchSettingItem(
                        title = "ダークモードを使用",
                        subtitle = if (useDarkMode) "ON: ダークモードを使用中" else "OFF: ライトモードを使用中",
                        checked = useDarkMode,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                context.dataStore.edit { preferences ->
                                    preferences[DARK_MODE_KEY] = enabled
                                }
                            }
                        }
                    )
                }

                item {
                    ColorPickerLauncherItem(
                        currentColor = currentHsv.copy(alpha = 1f).toColor(),
                        onClick = { showColorPickerDialog = true }
                    )
                }
            }

            item { Divider() }
            item { SectionHeader("ぼかし・透明度") }
            item {
                SwitchSettingItem(
                    title = "背景のぼかし",
                    subtitle = "キーボードの背景をぼかします",
                    checked = isBlurEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            context.dataStore.edit { preferences ->
                                preferences[BLUR_ENABLED_KEY] = enabled
                            }
                        }
                    }
                )
            }

            if (isBlurEnabled) {
                item {
                    InlineSliderItem(
                        title = "背景の透明度 (Alpha)",
                        valueText = "${(localAlpha * 100).roundToInt()}%",
                        value = localAlpha,
                        onValueChange = { localAlpha = it },
                        onValueChangeFinished = {
                            scope.launch {
                                context.dataStore.edit { preferences ->
                                    preferences[ALPHA_KEY] = localAlpha
                                }
                            }
                        },
                        valueRange = 0f..1f
                    )
                }

                item {
                    InlineSliderItem(
                        title = "ぼかしの強さ (Blur Radius)",
                        valueText = "${localBlurRadius.roundToInt()} px",
                        value = localBlurRadius,
                        onValueChange = { localBlurRadius = it },
                        onValueChangeFinished = {
                            scope.launch {
                                context.dataStore.edit { preferences ->
                                    preferences[BLUR_RADIUS_KEY] = localBlurRadius.roundToInt()
                                }
                            }
                        },
                        valueRange = 1f..100f
                    )
                }
            }
        }
    }
}

/**
 * カラーピッカーダイアログを開くための設定項目
 */
@Composable
private fun ColorPickerLauncherItem(
    currentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text("テーマカラーの選択") },
        supportingContent = { Text("タップしてカラーピッカーを開く") },
        trailingContent = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(currentColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        },
        modifier = modifier.clickable { onClick() }
    )
}
