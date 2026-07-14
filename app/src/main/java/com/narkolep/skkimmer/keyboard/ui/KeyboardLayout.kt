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
import com.narkolep.skkimmer.keyboard.ui.layouts.FlickKanaMap.flickLayout
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
import com.narkolep.skkimmer.keyboard.KeyboardState
import com.narkolep.skkimmer.keyboard.ui.layouts.QwertyMap.keyDefinitions
import com.narkolep.skkimmer.keyboard.ui.layouts.QwertyMap.numericKeyDefinitions
import com.narkolep.skkimmer.keyboard.ui.layouts.NumericMap
import com.narkolep.skkimmer.keyboard.ui.components.FlickKey
import com.composables.icons.lucide.R.drawable.lucide_ic_space
import com.composables.icons.lucide.R.drawable.lucide_ic_square_asterisk
import com.composables.icons.lucide.R.drawable.lucide_ic_square_dashed
import com.composables.icons.lucide.R.drawable.lucide_ic_square_library

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun KeyboardLayout(
    uiState: KeyboardState,
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
    val keyboardTextColor = colorScheme.onBackground
    val keyboardFlickColor = colorScheme.onSecondary
    val keyboardActionColor = colorScheme.primary
    val keyboardActionTextColor = colorScheme.onPrimary

    val isShifted = uiState.shiftState != ShiftState.LOWERCASE

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(keyboardBackgroundColor) // キーボード全体の背景色
            .padding(bottom = bottomPadding.dp)
    ) {
        /* 絵文字入力画面 */
        if (uiState.inputMode == InputMode.EMOJI) {
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
            return
        }

        /* キーボード */
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            /* 候補/数字バー */
            Box(modifier = Modifier.fillMaxWidth().height((keyboardHeight * 0.8).dp)) {
                if (!uiState.isFlick && uiState.candidates.isEmpty() && uiState.inputMode != InputMode.NUMERIC) {
                    Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                        numericKeyDefinitions.forEach { config ->
                            SkkKey(
                                mainText = config.main,
                                modifier = Modifier.weight(1f),
                                keyColor = keyboardBackgroundColor,
                                textColor = keyboardTextColor.copy(alpha = 0.8f),
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

            /* テンキー */
            if (uiState.inputMode == InputMode.NUMERIC) {
                NumericMap.numericLayout.forEach { rowKeys ->
                    Row(modifier = Modifier.fillMaxWidth().height(keyboardHeight.dp)) {
                        rowKeys.forEach { config ->
                            SkkKey(
                                mainText = config.label,
                                modifier = Modifier.weight(1f),
                                keyColor =
                                    when (config.color) {
                                        NumericMap.KeyColor.Background -> keyboardBackgroundColor
                                        NumericMap.KeyColor.Action -> keyboardActionColor
                                        else -> keyboardButtonColor
                                    },
                                textColor =
                                    when (config.color) {
                                        NumericMap.KeyColor.Action -> keyboardActionTextColor
                                        else -> keyboardTextColor
                                    },
                                iconResId = config.icon,
                                keyboardHeight = keyboardHeight,
                                cornerShape = if (config.action == KeyboardAction.Enter) (keyboardHeight*0.5).dp else 8.dp,
                                keyRepeat = config.keyRepeat,
                                onClick = {
                                    if (config.action != null) onActionClick(config.action)
                                    else onKeyClick(config.label)
                                }
                            )
                        }
                    }
                }
                return
            }

            /* フリックキーボード */
            if (uiState.isFlick) {
                flickLayout.forEach { rowKeys ->
                    Row(modifier = Modifier.fillMaxWidth().height(keyboardHeight.dp)) {
                        rowKeys.forEach { config ->
                            if (config.action != null) {
                                val keyColor = when (config.action) {
                                    KeyboardAction.Ctrl -> if (uiState.isCtrlPressed) keyboardActionColor else keyboardBackgroundColor
                                    KeyboardAction.Shift -> if (isShifted) keyboardActionColor else keyboardBackgroundColor
                                    KeyboardAction.Enter -> keyboardActionColor
                                    KeyboardAction.Dakuten -> keyboardButtonColor
                                    else -> keyboardBackgroundColor
                                }
                                val actionIcon = if (config.action == KeyboardAction.Space) {
                                        when (uiState.skkState) {
                                            SkkState.MIDASHI -> lucide_ic_square_dashed
                                            SkkState.OKURIGANA -> lucide_ic_square_asterisk
                                            SkkState.HENKAN -> lucide_ic_square_library
                                            else -> lucide_ic_space
                                        }
                                    } else config.iconResId

                                SkkKey(
                                    mainText = config.hiraLabel,
                                    modifier = Modifier.weight(1f),
                                    keyColor = keyColor,
                                    textColor = if (keyColor == keyboardActionColor) keyboardActionTextColor else keyboardTextColor,
                                    iconResId = actionIcon,
                                    keyboardHeight = keyboardHeight,
                                    cornerShape = if (config.action == KeyboardAction.Enter) (keyboardHeight*0.5).dp else 8.dp,
                                    keyRepeat = config.keyRepeat,
                                    onClick = { onActionClick(config.action) }
                                )
                            } else {
                                FlickKey(
                                    config = config,
                                    modifier = Modifier.weight(1f),
                                    displayText =
                                        when (uiState.inputMode) {
                                            InputMode.KATAKANA -> config.kataLabel
                                            InputMode.HALF_KATAKANA -> config.halfLabel
                                            else -> config.hiraLabel
                                        },
                                    keyColor =
                                        when (config.hiraLabel) {
                                            "Q" -> keyboardBackgroundColor
                                            else -> keyboardButtonColor
                                        },
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
                return
            }

            /* qwertyキーボード */
            keyDefinitions.forEach { rowKeys ->
                Row(modifier = Modifier.fillMaxWidth().height(keyboardHeight.dp)) {
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
                                        InputMode.HALF_ASCII -> "半角英数"
                                        InputMode.FULL_ASCII -> "全角英数"
                                        InputMode.HIRAGANA -> when (uiState.skkState) {
                                            SkkState.NORMAL -> jisyoKey + "NORMAL"
                                            SkkState.MIDASHI -> jisyoKey + "見出し語"
                                            SkkState.OKURIGANA -> jisyoKey + "送り仮名"
                                            SkkState.HENKAN -> jisyoKey + "変換"
                                            SkkState.ABBREV -> jisyoKey + "abbreviation"
                                        }
                                        InputMode.KATAKANA -> "カタカナ"
                                        InputMode.HALF_KATAKANA -> "____ｶﾀｶﾅ"
                                    }
                                }
                                else -> {}
                            }

                            val keyColor = when (config.action) {
                                KeyboardAction.Shift -> if (isShifted) keyboardActionColor else keyboardBackgroundColor
                                KeyboardAction.Backspace -> keyboardBackgroundColor
                                KeyboardAction.Ctrl -> if (uiState.isCtrlPressed) keyboardActionColor else keyboardBackgroundColor
                                KeyboardAction.Space -> keyboardButtonColor
                                KeyboardAction.Enter -> keyboardActionColor
                                else -> keyboardButtonColor
                            }

                            SkkKey(
                                mainText = mainText,
                                modifier = Modifier.weight(config.weight),
                                textSize = config.textSize,
                                keyColor = keyColor,
                                textColor = if (keyColor == keyboardActionColor) keyboardActionTextColor else keyboardTextColor,
                                keyboardHeight = keyboardHeight,
                                cornerShape = if (config.action == KeyboardAction.Enter) (keyboardHeight*0.5).dp else 8.dp,
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