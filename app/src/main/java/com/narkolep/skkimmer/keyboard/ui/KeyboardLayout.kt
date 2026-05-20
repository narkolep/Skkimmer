package com.narkolep.skkimmer.keyboard.ui

import kotlinx.coroutines.flow.map
import android.content.res.Configuration
import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.narkolep.skkimmer.keyboard.mappings.FlickKanaMap.flickLayout
import com.narkolep.skkimmer.keyboard.ui.components.CandidateBar
import com.narkolep.skkimmer.keyboard.ui.components.SkkKey
import com.narkolep.skkimmer.data.EmojiManager
import com.narkolep.skkimmer.keyboard.ui.components.EmojiPicker
import com.narkolep.skkimmer.dataStore
import com.narkolep.skkimmer.THEME_KEY
import com.narkolep.skkimmer.KEYBOARD_HEIGHT_KEY
import com.narkolep.skkimmer.KEYBOARD_HEIGHT_LANDSCAPE_KEY
import com.narkolep.skkimmer.KEYBOARD_HEIGHT_BOTTOM_PADDING
import com.narkolep.skkimmer.keyboard.InputMode
import com.narkolep.skkimmer.keyboard.KeyboardAction
import com.narkolep.skkimmer.keyboard.ShiftState
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.mappings.KeyboardMap.keyDefinitions
import com.narkolep.skkimmer.keyboard.mappings.KeyboardMap.numericKeyDefinitions
import com.narkolep.skkimmer.keyboard.mappings.NumericMap
import com.narkolep.skkimmer.keyboard.ui.components.FlickKey
import com.composables.icons.lucide.R.drawable.lucide_ic_space
import com.composables.icons.lucide.R.drawable.lucide_ic_square_asterisk
import com.composables.icons.lucide.R.drawable.lucide_ic_square_dashed
import com.composables.icons.lucide.R.drawable.lucide_ic_square_library

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun KeyboardLayout(
    uiState: SkkUIState,
    categories: List<EmojiManager.Category>,
    onKeyClick: (String) -> Unit,
    onActionClick: (KeyboardAction) -> Unit
) {
    val context = LocalContext.current

    /* 端末の向きを取得 */
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val portraitHeight by context.dataStore.data
        .map { preferences -> preferences[KEYBOARD_HEIGHT_KEY] ?: 55f }
        .collectAsState(initial = 55f)

    val landscapeHeight by context.dataStore.data
        .map { preferences -> preferences[KEYBOARD_HEIGHT_LANDSCAPE_KEY] ?: 45f }
        .collectAsState(initial = 45f)

    val bottomPadding by context.dataStore.data
        .map { preferences -> preferences[KEYBOARD_HEIGHT_BOTTOM_PADDING] ?: 48f }
        .collectAsState(initial = 48f)

    val keyboardHeight = if (isLandscape) landscapeHeight else portraitHeight

    val themeMode by context.dataStore.data
        .map { preferences -> preferences[THEME_KEY] ?: "システム" }
        .collectAsState(initial = "システム")

    val isDarkTheme = when (themeMode) {
        "ダーク" -> true
        "ライト" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isDarkTheme) darkColorScheme() else lightColorScheme()
    }

    /* ボタンの色 */
    val keyboardBackgroundColor = colorScheme.surfaceDim
    val keyboardButtonColor = colorScheme.surfaceBright
    val keyboardFlickColor = colorScheme.onPrimary
    val keyboardTextColor = colorScheme.onBackground
    val keyboardActionColor = colorScheme.primaryFixedDim
    val keyboardActionTextColor = colorScheme.onPrimary

    val isShifted = uiState.shiftState != ShiftState.LOWERCASE

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(keyboardBackgroundColor) // キーボード全体の背景色
            .padding(bottom = bottomPadding.dp)
    ) {
        when (uiState.inputMode) {
            InputMode.EMOJI -> {
                /**
                 * 絵文字入力画面
                 **/
                EmojiPicker(
                    backgroundColor = keyboardBackgroundColor,
                    textColor = keyboardTextColor,
                    actionColor = keyboardActionColor,
                    actionTextColor = keyboardActionTextColor,
                    height = (keyboardHeight * 4.8f),
                    categories = categories,
                    onBackToKeyboard = { onActionClick(KeyboardAction.ToggleKeyboard) },
                    onEmojiSelected = { emoji -> onKeyClick(emoji) }
                )
            }
            InputMode.NUMERIC -> {
                /**
                 * テンキー
                 **/
                Column(modifier = Modifier.padding(horizontal = 2.dp)) {
                    NumericMap.numericLayout.forEach { rowKeys ->
                        Row(modifier = Modifier.fillMaxWidth().height(keyboardHeight.dp)) {
                            rowKeys.forEach { config ->
                                SkkKey(
                                    mainText = config.label,
                                    modifier = Modifier.weight(1f),
                                    keyColor = if (config.color) keyboardBackgroundColor else keyboardButtonColor,
                                    textColor = keyboardTextColor,
                                    iconResId = config.icon,
                                    keyboardHeight = keyboardHeight,
                                    keyRepeat = config.keyRepeat,
                                    onClick = {
                                        if (config.action != null) onActionClick(config.action)
                                        else onKeyClick(config.label)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    /**
                     * 候補/数字バー
                     **/
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height((keyboardHeight * 0.8).dp)) {
                        if (!uiState.isFlick) {
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()) {
                                numericKeyDefinitions.forEach { config ->
                                    SkkKey(
                                        mainText = config.main,
                                        modifier = Modifier.weight(1f),
                                        keyColor = keyboardBackgroundColor,
                                        textColor = keyboardTextColor,
                                        keyboardHeight = keyboardHeight,
                                        onClick = { onKeyClick(config.main) }
                                    )
                                }
                            }
                        } else if (uiState.candidates.isNotEmpty()) {
                            CandidateBar(
                                backgroundColor = keyboardBackgroundColor,
                                selectedBackgroundColor = keyboardButtonColor,
                                selectedTextColor = keyboardTextColor,
                                candidates = uiState.candidates,
                                selectedIndex = uiState.selectedIndex,
                                onCandidateClick = { index -> onActionClick(KeyboardAction.CandidateIndex(index)) }
                            )
                        }
                    }

                    if (uiState.isFlick) {
                        /**
                         * フリックキーボード
                         **/
                        flickLayout.forEach { rowKeys ->
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .height(keyboardHeight.dp)) {
                                rowKeys.forEach { config ->
                                    if (config.action != null) {
                                        /* 機能キー (actionがある場合) */
                                        val actionColor = when (config.action) {
                                            KeyboardAction.Ctrl -> if (uiState.isCtrlPressed) keyboardActionColor else keyboardBackgroundColor
                                            KeyboardAction.Shift -> if (isShifted) keyboardActionColor else keyboardBackgroundColor
                                            KeyboardAction.Enter -> keyboardActionColor
                                            KeyboardAction.Dakuten -> keyboardButtonColor
                                            else -> keyboardBackgroundColor
                                        }
                                        var actionIcon = config.iconResId
                                        var actionText = config.label
                                        if (config.action == KeyboardAction.Space) {
                                            actionIcon = when (uiState.inputMode) {
                                                InputMode.HIRAGANA -> when (uiState.skkState) {
                                                    SkkState.NORMAL -> lucide_ic_space
                                                    SkkState.MIDASHI -> lucide_ic_square_dashed
                                                    SkkState.OKURIGANA -> lucide_ic_square_asterisk
                                                    SkkState.HENKAN -> lucide_ic_square_library
                                                    else -> lucide_ic_space
                                                }

                                                else -> null
                                            }
                                            actionText = when (uiState.inputMode) {
                                                InputMode.HALF_KATAKANA -> "__ｶﾅ"
                                                InputMode.KATAKANA -> "カナ"
                                                InputMode.HALF_ASCII -> "半角"
                                                InputMode.FULL_ASCII -> "全角"
                                                else -> "Space"
                                            }
                                        }

                                        SkkKey(
                                            mainText = actionText,
                                            modifier = Modifier.weight(1f),
                                            keyColor = actionColor,
                                            textColor = if (actionColor == keyboardActionColor) keyboardActionTextColor else keyboardTextColor,
                                            iconResId = actionIcon,
                                            keyboardHeight = keyboardHeight,
                                            keyRepeat = config.keyRepeat,
                                            onClick = { onActionClick(config.action) }
                                        )
                                    } else {
                                        /* フリックキー */
                                        val flickColor = when (config.label) {
                                            "Q" -> keyboardBackgroundColor
                                            else -> keyboardButtonColor
                                        }

                                        FlickKey(
                                            config = config,
                                            modifier = Modifier.weight(1f),
                                            displayText = config.label,
                                            keyColor = flickColor,
                                            textColor = keyboardTextColor,
                                            backgroundColor = keyboardBackgroundColor,
                                            actionColor = keyboardActionColor,
                                            iconResId = config.iconResId,
                                            isCtrlPressed = uiState.isCtrlPressed,
                                            onInput = { text ->
                                                /* nnなど、複数文字の場合があるため */
                                                text.forEach { keyId -> onKeyClick(keyId.toString()) }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        /**
                         * qwertyキーボード
                         **/
                        keyDefinitions.forEach { rowKeys ->
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .height(keyboardHeight.dp)) {
                                rowKeys.forEach { config ->
                                    if (config.action != null) {
                                        var mainText = config.main
                                        when(config.action) {
                                            KeyboardAction.Shift -> {
                                                mainText = when (uiState.shiftState) {
                                                    ShiftState.LOWERCASE -> "Shift"
                                                    ShiftState.SHIFTED -> "Shift"
                                                    ShiftState.CAPS_LOCK -> "Caps\nLock"
                                                }
                                            }
                                            KeyboardAction.Space -> {
                                                val jisyoKey = if (uiState.tourokuFlag.isNotEmpty()) "辞書登録 / " else ""
                                                mainText = when (uiState.inputMode) {
                                                    InputMode.HALF_ASCII -> "SKK"
                                                    InputMode.FULL_ASCII -> "全英"
                                                    InputMode.HIRAGANA -> when (uiState.skkState) {
                                                        SkkState.NORMAL -> jisyoKey + "NORMAL"
                                                        SkkState.MIDASHI -> jisyoKey + "見出し語"
                                                        SkkState.OKURIGANA -> jisyoKey + "送り仮名"
                                                        SkkState.HENKAN -> jisyoKey + "変換"
                                                        SkkState.ABBREV -> jisyoKey + "abbreviation"
                                                    }
                                                    InputMode.KATAKANA -> "カナ"
                                                    InputMode.HALF_KATAKANA -> "__ｶﾅ"
                                                }
                                            }
                                            else -> {}
                                        }

                                        val keyColor = when (config.action) {
                                            KeyboardAction.Shift -> {
                                                if (uiState.shiftState == ShiftState.LOWERCASE) keyboardBackgroundColor
                                                else keyboardActionColor
                                            }
                                            KeyboardAction.Backspace -> keyboardBackgroundColor
                                            KeyboardAction.Ctrl -> {
                                                if (!uiState.isCtrlPressed) keyboardBackgroundColor
                                                else keyboardActionColor
                                            }
                                            KeyboardAction.Space -> keyboardButtonColor
                                            KeyboardAction.Enter -> keyboardActionColor
                                            else -> keyboardButtonColor
                                        }

                                        val textColor = when (config.action) {
                                            KeyboardAction.Shift -> {
                                                if (uiState.shiftState == ShiftState.LOWERCASE) keyboardTextColor
                                                else keyboardActionTextColor
                                            }
                                            KeyboardAction.Backspace -> keyboardTextColor
                                            KeyboardAction.Ctrl -> {
                                                if (!uiState.isCtrlPressed) keyboardTextColor
                                                else keyboardActionTextColor
                                            }
                                            KeyboardAction.Space -> keyboardActionColor
                                            KeyboardAction.Enter -> keyboardActionTextColor
                                            else -> keyboardTextColor
                                        }

                                        SkkKey(
                                            mainText = mainText,
                                            modifier = Modifier.weight(config.weight),
                                            textSize = config.textSize,
                                            keyColor = keyColor,
                                            textColor = textColor,
                                            keyboardHeight = keyboardHeight,
                                            keyRepeat = config.keyRepeat
                                        ) { onActionClick(config.action) }
                                    } else {
                                        SkkKey(
                                            mainText = if (isShifted) config.main.uppercase() else config.main,
                                            flickText = if (isShifted) config.shiftFlick else config.flick,
                                            modifier = Modifier.weight(config.weight),
                                            keyColor = keyboardButtonColor,
                                            textColor = keyboardTextColor,
                                            flickColor = keyboardFlickColor,
                                            keyboardHeight = keyboardHeight,
                                            spaceLeftRight = config.padding,
                                            weight = config.weight,
                                            onFlick = { onKeyClick(if (isShifted) config.shiftFlick else config.flick) },
                                            onClick = { onKeyClick(config.main) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}