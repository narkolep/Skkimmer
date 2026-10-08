package com.narkolep.skkimmer

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.booleanPreferencesKey

/* data */
import com.narkolep.skkimmer.data.HistoryViewModel
import com.narkolep.skkimmer.data.HistoryDao
import com.narkolep.skkimmer.data.AppDatabase

/* screens */
import com.narkolep.skkimmer.ui.screens.DictionaryListScreen
import com.narkolep.skkimmer.ui.screens.SettingsScreen
import com.narkolep.skkimmer.ui.screens.ThemeColorScreen
import com.narkolep.skkimmer.ui.screens.UserDictionaryScreen

/* theme */
import com.narkolep.skkimmer.ui.theme.AppTheme

/* DataStoreの作成とキーの定義 */
val Context.dataStore by preferencesDataStore(name = "settings")

val THEME_SELECT_KEY = booleanPreferencesKey("theme_select")
val HUE_KEY = floatPreferencesKey("theme_color_hue")
val SATURATION_KEY = floatPreferencesKey("theme_color_saturation")
val VALUE_KEY = floatPreferencesKey("theme_color_value")
val ALPHA_KEY = floatPreferencesKey("theme_color_alpha")
val KEYBOARD_HEIGHT_KEY = floatPreferencesKey("keyboard_height")
val KEYBOARD_HEIGHT_LANDSCAPE_KEY = floatPreferencesKey("keyboard_height_landscape") // 横向き用
val KEYBOARD_PADDING_KEY = floatPreferencesKey("keyboard_padding")
val KEYBOARD_BOTTOM_PADDING_KEY = floatPreferencesKey("keyboard_bottom_padding")

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* AppDatabaseからデータベースのインスタンスを取得する */
        val db = AppDatabase.getDatabase(this)
        /* データベースから HistoryDao を取り出す */
        val historyDao = db.historyDao()

        setContent {
            App(dao = historyDao)
        }
    }
}

/**
 * アプリ本体の定義
 * */
@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun App(dao: HistoryDao) {
    val navController = rememberNavController()

    // ダークモードにするか判定
    val useDarkTheme = isSystemInDarkTheme()

    // Factoryを1箇所で定義
    val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(dao) as T
        }
    }

    // ViewModelをApp内で作成する
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)

    AppTheme (useDarkTheme = useDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = "settings",

                // 新しい画面を開くとき（進む）のアニメーション
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth }, // 右端から
                        animationSpec = tween(300) // 300ミリ秒かけて
                    ) + fadeIn(animationSpec = tween(300)) // 同時にフェードイン
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 3 }, // 少しだけ左へ押し出される
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                // 前の画面に戻るとき（戻る）のアニメーション
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 3 }, // 左側から少し戻ってくる
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth }, // 右端へ消えていく
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                composable("settings") {
                    SettingsScreen(navController)
                }

                composable("dictionary_list") {
                    DictionaryListScreen()
                }

                composable("user_dict") {
                    UserDictionaryScreen(historyViewModel)
                }

                composable("color") {
                    ThemeColorScreen()
                }
            }
        }
    }
}