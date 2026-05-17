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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/* data */
import com.narkolep.skkimmer.data.HistoryViewModel
import com.narkolep.skkimmer.data.HistoryDao
import com.narkolep.skkimmer.data.AppDatabase

/* screens */
import com.narkolep.skkimmer.ui.screens.DictionaryListScreen
import com.narkolep.skkimmer.ui.screens.SettingsScreen
import com.narkolep.skkimmer.ui.screens.UserDictionaryScreen

/* theme */
import com.narkolep.skkimmer.ui.theme.AppTheme

/* DataStoreの作成とキーの定義 */
val Context.dataStore by preferencesDataStore(name = "settings")
val THEME_KEY = stringPreferencesKey("theme_mode")
val KEYBOARD_HEIGHT_KEY = floatPreferencesKey("keyboard_height")
val KEYBOARD_HEIGHT_LANDSCAPE_KEY = floatPreferencesKey("keyboard_height_landscape") // 横向き用
val KEYBOARD_HEIGHT_BOTTOM_PADDING = floatPreferencesKey("keyboard_bottom_padding")

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AppDatabaseからデータベースのインスタンスを取得する
        val db = AppDatabase.getDatabase(this)
        // データベースから HistoryDao を取り出す
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
    val context = LocalContext.current

    // DataStoreからテーマの設定を読み込む（初期値は "システム"）
    val themeMode by context.dataStore.data
        .map { preferences -> preferences[THEME_KEY] ?: "システム" }
        .collectAsState(initial = "システム")

    // 端末自体のダークモード設定を取得
    val isSystemDark = isSystemInDarkTheme()

    // DataStoreの設定値に応じて、実際にダークモードにするか判定
    val useDarkTheme = when (themeMode) {
        "ダーク" -> true
        "ライト" -> false
        else -> isSystemDark
    }

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
            }
        }
    }
}