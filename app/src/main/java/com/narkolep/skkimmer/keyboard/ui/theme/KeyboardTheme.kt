package com.narkolep.skkimmer.keyboard.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.narkolep.skkimmer.ALPHA_KEY
import com.narkolep.skkimmer.HUE_KEY
import com.narkolep.skkimmer.SATURATION_KEY
import com.narkolep.skkimmer.THEME_SELECT_KEY
import com.narkolep.skkimmer.VALUE_KEY
import com.narkolep.skkimmer.dataStore
import kotlinx.coroutines.flow.map

val LocalKeyboardColors = staticCompositionLocalOf {
    /* default */
    KeyboardColors(
        background = Color(0xFF1C1B1F),
        keyBackground = Color(0xFF2B2930),
        keyText = Color(0xFFE6E1E5),
        flickKeyBackground = Color(0xFF4A4458),
        specialKeyBackground = Color(0xFFD0BCFF),
        specialKeyText = Color(0xFF381E72),
    )
}

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun KeyboardTheme(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    /* カストムテーマを使用するかどうか */
    val isCustomTheme by context.dataStore.data
        .map { preferences -> preferences[THEME_SELECT_KEY] ?: false }
        .collectAsState(initial = false)

    /* カスタム色 */
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

    /* Material Color */
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    }

    val colors = if (isCustomTheme) {
        keyboardColors(currentHsv)
    } else {
        KeyboardColors(
            background = colorScheme.surfaceDim,
            keyBackground = colorScheme.surfaceBright,
            keyText = colorScheme.onBackground,
            flickKeyBackground = colorScheme.onSecondary,
            specialKeyBackground = colorScheme.primary,
            specialKeyText = colorScheme.onPrimary
        )
    }

    CompositionLocalProvider(
        LocalKeyboardColors provides colors,
        content = content
    )
}