package com.narkolep.skkimmer.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import com.narkolep.skkimmer.ALPHA_KEY
import com.narkolep.skkimmer.HUE_KEY
import com.narkolep.skkimmer.SATURATION_KEY
import com.narkolep.skkimmer.THEME_SELECT_KEY
import com.narkolep.skkimmer.VALUE_KEY
import com.narkolep.skkimmer.dataStore
import com.narkolep.skkimmer.keyboard.ui.theme.LocalKeyboardColors
import com.narkolep.skkimmer.ui.components.Divider
import com.narkolep.skkimmer.ui.components.HarmonyColorPicker
import com.narkolep.skkimmer.keyboard.ui.theme.HSVColor
import com.narkolep.skkimmer.ui.components.SectionHeader
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun ThemeColorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // DataStoreから現在のテーマを読み取る
    val isCustomTheme by context.dataStore.data
        .map { preferences -> preferences[THEME_SELECT_KEY] ?: false }
        .collectAsState(initial = false)

    val currentHsv by context.dataStore.data
        .map { preferences ->
            HSVColor(
                hue = preferences[HUE_KEY] ?: 0f,
                saturation = preferences[SATURATION_KEY] ?: 0f,
                value = preferences[VALUE_KEY] ?: 0f,
                alpha = preferences[ALPHA_KEY] ?: 1f,
            )
        }
        .collectAsState(initial = HSVColor.Default)

    val colors = LocalKeyboardColors.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("テーマカラー") })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item { Divider() }
            item {
                ListItem(
                    headlineContent = { Text("カスタムテーマを使用する") },
                    supportingContent = { Text("OFFのときはMaterial3の色を使用します") },
                    trailingContent = {
                        Switch(
                            checked = isCustomTheme,
                            onCheckedChange = {
                                scope.launch {
                                    context.dataStore.edit { preferences ->
                                        preferences[THEME_SELECT_KEY] = it
                                    }
                                }
                            }
                        )
                    }
                )
            }

            item { Divider() }
            item { SectionHeader("テーマカラーの選択") }
            item {
                HarmonyColorPicker(
                    value = currentHsv,
                    onValueChanged = { newColor ->
                        scope.launch {
                            context.dataStore.edit { preferences ->
                                preferences[HUE_KEY] = newColor.hue
                                preferences[SATURATION_KEY] = newColor.saturation
                                preferences[VALUE_KEY] = newColor.value
                                preferences[ALPHA_KEY] = newColor.alpha
                            }
                        }
                    }
                )
            }

            item { Divider() }
            item { SectionHeader("プレビュー") }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.background(colors.background)
                    ) {
                        Text(
                            text = "背景",
                            color = colors.keyText,
                            fontSize = 20.sp
                        )
                    }
                    Box(
                        modifier = Modifier.background(colors.keyBackground)
                    ) {
                        Text(
                            text = "一般キー",
                            color = colors.keyText,
                            fontSize = 20.sp
                        )
                    }
                    Box(
                        modifier = Modifier.background(colors.flickKeyBackground)
                    ) {
                        Text(
                            text = "ポップアップ",
                            color = colors.keyText,
                            fontSize = 20.sp
                        )
                    }
                    Box(
                        modifier = Modifier.background(colors.specialKeyBackground)
                    ) {
                        Text(
                            text = "アクションキー",
                            color = colors.specialKeyText,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}