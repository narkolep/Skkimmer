package com.narkolep.skkimmer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/* data */
import com.narkolep.skkimmer.data.HistoryViewModel
import com.narkolep.skkimmer.data.db.AppDatabase
import com.narkolep.skkimmer.data.db.dao.HistoryDao

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
val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
val BLUR_ENABLED_KEY = booleanPreferencesKey("blur_enabled")
val BLUR_RADIUS_KEY = intPreferencesKey("blur_radius")
val HUE_KEY = floatPreferencesKey("theme_color_hue")
val SATURATION_KEY = floatPreferencesKey("theme_color_saturation")
val VALUE_KEY = floatPreferencesKey("theme_color_value")
val ALPHA_KEY = floatPreferencesKey("theme_color_alpha")
val KEYBOARD_HEIGHT_KEY = floatPreferencesKey("keyboard_height")
val KEYBOARD_HEIGHT_LANDSCAPE_KEY = floatPreferencesKey("keyboard_height_landscape") // 横向き用
val KEYBOARD_PADDING_KEY = floatPreferencesKey("keyboard_padding")
val KEYBOARD_BOTTOM_PADDING_KEY = floatPreferencesKey("keyboard_bottom_padding")
val HAPTIC_ENABLED_KEY = booleanPreferencesKey("haptic_enabled")
val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
@Composable
fun App(dao: HistoryDao) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // ダークモードにするか判定
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

    val useDarkTheme = if (!isCustomTheme) isSystemInDarkTheme() else useDarkMode

    // Factoryを1箇所で定義
    val factory = remember(dao) {
        viewModelFactory {
            initializer {
                HistoryViewModel(dao)
            }
        }
    }

    // ViewModelをApp内で作成する
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)

    AppTheme(useDarkTheme = useDarkTheme) {
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
                },
                // 予測型 戻るアニメーション
                predictivePopEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 3 }, // 左側から少し戻ってくる
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                predictivePopExitTransition = {
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