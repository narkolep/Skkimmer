package com.narkolep.skkimmer.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import com.narkolep.skkimmer.dataStore
import com.narkolep.skkimmer.THEME_KEY
import com.narkolep.skkimmer.KEYBOARD_HEIGHT_KEY
import com.narkolep.skkimmer.KEYBOARD_HEIGHT_LANDSCAPE_KEY
import com.narkolep.skkimmer.KEYBOARD_HEIGHT_BOTTOM_PADDING
import com.narkolep.skkimmer.ui.components.Divider
import com.narkolep.skkimmer.ui.components.KeyboardHeightSliderItem
import com.narkolep.skkimmer.ui.components.SectionHeader
import com.narkolep.skkimmer.ui.components.SettingItem
import com.narkolep.skkimmer.ui.components.ThemeSettingItem
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity

@SuppressLint("FlowOperatorInvokedInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // DataStoreから現在のテーマを読み取る
    val currentTheme by context.dataStore.data
        .map { preferences -> preferences[THEME_KEY] ?: "システム" }
        .collectAsState(initial = "システム")

    // 縦向きの高さ
    val portraitHeight by context.dataStore.data
        .map { preferences -> preferences[KEYBOARD_HEIGHT_KEY] ?: 55f } // デフォルトは250dp
        .collectAsState(initial = 55f)

    // 横向きの高さ
    val landscapeHeight by context.dataStore.data
        .map { preferences -> preferences[KEYBOARD_HEIGHT_LANDSCAPE_KEY] ?: 45f }
        .collectAsState(initial = 45f)

    // パディング
    val bottomPadding by context.dataStore.data
        .map { preferences -> preferences[KEYBOARD_HEIGHT_BOTTOM_PADDING] ?: 47f }
        .collectAsState(initial = 47f)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("設定") })
        }
    ) { padding ->

        LazyColumn(modifier = Modifier.padding(padding)) {

            item { Divider() }
            item { SectionHeader("辞書") }
            item {
                SettingItem("辞書一覧", "辞書を追加できます") {
                    navController.navigate("dictionary_list")
                }
            }
            item {
                SettingItem("変換履歴", "保存されている履歴データを確認できます") {
                    navController.navigate("user_dict")
                }
            }

            item { Divider() }
            item { SectionHeader("デザイン") }
            item {
                ThemeSettingItem(
                    selected = currentTheme,
                    onThemeSelected = { newTheme ->
                        scope.launch {
                            context.dataStore.edit { preferences ->
                                preferences[THEME_KEY] = newTheme
                            }
                        }
                    }
                )
            }
            item {
                KeyboardHeightSliderItem(
                    title = "キーボードの高さ（縦）",
                    currentHeight = portraitHeight,
                    valueRange = 40f..70f,
                    onHeightChanged = { newHeight ->
                        scope.launch {
                            context.dataStore.edit { preferences ->
                                preferences[KEYBOARD_HEIGHT_KEY] = newHeight
                            }
                        }
                    }
                )
            }
            item {
                KeyboardHeightSliderItem(
                    title = "キーボードの高さ（横）",
                    currentHeight = landscapeHeight,
                    valueRange = 30f..60f,
                    onHeightChanged = { newHeight ->
                        scope.launch {
                            context.dataStore.edit { preferences ->
                                preferences[KEYBOARD_HEIGHT_LANDSCAPE_KEY] = newHeight
                            }
                        }
                    }
                )
            }
            item {
                KeyboardHeightSliderItem(
                    title = "下部のスペース",
                    currentHeight = bottomPadding,
                    valueRange = 0f..70f,
                    onHeightChanged = { newHeight ->
                        // 保存処理
                        scope.launch {
                            context.dataStore.edit { preferences ->
                                preferences[KEYBOARD_HEIGHT_BOTTOM_PADDING] = newHeight
                            }
                        }
                    }
                )
            }

            item { Divider() }
            item { SectionHeader("このアプリについて") }
            item {
                SettingItem("Open source licenses", "サードパーティのライセンスを確認できます") {
                    val intent = Intent(context, OssLicensesMenuActivity::class.java)
                    context.startActivity(intent)
                }
            }
        }
    }
}