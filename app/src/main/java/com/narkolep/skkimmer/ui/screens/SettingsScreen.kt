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
import com.narkolep.skkimmer.KEYBOARD_HEIGHT_KEY
import com.narkolep.skkimmer.KEYBOARD_HEIGHT_LANDSCAPE_KEY
import com.narkolep.skkimmer.KEYBOARD_BOTTOM_PADDING_KEY
import com.narkolep.skkimmer.KEYBOARD_PADDING_KEY
import com.narkolep.skkimmer.ui.components.Divider
import com.narkolep.skkimmer.ui.components.DpSliderItem
import com.narkolep.skkimmer.ui.components.SectionHeader
import com.narkolep.skkimmer.ui.components.SettingItem
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

    // 縦向きの高さ
    val portraitHeight by context.dataStore.data
        .map { preferences -> preferences[KEYBOARD_HEIGHT_KEY] ?: 54f } // デフォルトは250dp
        .collectAsState(initial = 54f)

    // 横向きの高さ
    val landscapeHeight by context.dataStore.data
        .map { preferences -> preferences[KEYBOARD_HEIGHT_LANDSCAPE_KEY] ?: 44f }
        .collectAsState(initial = 44f)

    // 下端のパディング
    val bottomPadding by context.dataStore.data
        .map { preferences -> preferences[KEYBOARD_BOTTOM_PADDING_KEY] ?: 51f }
        .collectAsState(initial = 51f)

    // 左右のパディング
    val sidePadding by context.dataStore.data
        .map { preferences -> preferences[KEYBOARD_PADDING_KEY] ?: 0f }
        .collectAsState(initial = 0f)

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
                SettingItem("テーマカラー", "キーボードの色をカスタマイズできます") {
                    navController.navigate("color")
                }
            }
            item {
                DpSliderItem(
                    title = "キーボードの高さ（縦）",
                    currentValue = portraitHeight,
                    valueRange = 40f..70f,
                    steps = 10,
                    onValueChanged = { newValue ->
                        scope.launch {
                            context.dataStore.edit { preferences ->
                                preferences[KEYBOARD_HEIGHT_KEY] = newValue
                            }
                        }
                    }
                )
            }
            item {
                DpSliderItem(
                    title = "キーボードの高さ（横）",
                    currentValue = landscapeHeight,
                    valueRange = 30f..60f,
                    steps = 10,
                    onValueChanged = { newValue ->
                        scope.launch {
                            context.dataStore.edit { preferences ->
                                preferences[KEYBOARD_HEIGHT_LANDSCAPE_KEY] = newValue
                            }
                        }
                    }
                )
            }
            item {
                DpSliderItem(
                    title = "下部のスペース",
                    currentValue = bottomPadding,
                    valueRange = 0f..80f,
                    steps = 10,
                    onValueChanged = { newValue ->
                        scope.launch {
                            context.dataStore.edit { preferences ->
                                preferences[KEYBOARD_BOTTOM_PADDING_KEY] = newValue
                            }
                        }
                    }
                )
            }
            item {
                DpSliderItem(
                    title = "左右のスペース",
                    currentValue = sidePadding,
                    valueRange = 0f..50f,
                    steps = 10,
                    onValueChanged = { newValue ->
                        scope.launch {
                            context.dataStore.edit { preferences ->
                                preferences[KEYBOARD_PADDING_KEY] = newValue
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