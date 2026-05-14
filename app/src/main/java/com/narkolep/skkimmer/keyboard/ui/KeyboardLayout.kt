package com.narkolep.skkimmer.keyboard.ui

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
import com.narkolep.skkimmer.keyboard.mappings.KeyboardMap
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
import kotlinx.coroutines.flow.map
import android.content.res.Configuration
import com.narkolep.skkimmer.R
import com.narkolep.skkimmer.keyboard.InputMode
import com.narkolep.skkimmer.keyboard.KeyboardAction
import com.narkolep.skkimmer.keyboard.ShiftState
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.ui.components.FlickKey
import com.narkolep.skkimmer.keyboard.ui.components.NumericKeyboard

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

    /* DataStoreから現在のテーマ設定を読み込む（初期値は "システム"） */
    val themeMode by context.dataStore.data
        .map { preferences -> preferences[THEME_KEY] ?: "システム" }
        .collectAsState(initial = "システム")

    /* 設定値に基づいて、最終的にダークモードにするかどうかを決定 */
    val isDarkTheme = when (themeMode) {
        "ダーク" -> true
        "ライト" -> false
        else -> isSystemInDarkTheme()
    }

    /* Android 12 (API 31) 以上なら壁紙の色を取得、それ以外はデフォルトのテーマを使用 */
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isDarkTheme) darkColorScheme() else lightColorScheme()
    }

    /* ボタンの色の設定 */
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
            .padding(bottom = bottomPadding.dp) // paddingBottom
    ) {
        when (uiState.inputMode) {
            InputMode.EMOJI -> {
                EmojiPicker(
                    backgroundColor = keyboardBackgroundColor,
                    textColor = keyboardTextColor,
                    actionColor = keyboardActionColor,
                    actionTextColor = keyboardActionTextColor,
                    height = (keyboardHeight * 4.8f),
                    categories = categories,
                    onBackToKeyboard = { onActionClick(KeyboardAction.ToggleEmoji) },
                    onEmojiSelected = { emoji -> onKeyClick(emoji) }
                )
            }
            InputMode.NUMERIC -> {
                NumericKeyboard(
                    height = keyboardHeight,
                    backgroundColor = keyboardBackgroundColor,
                    buttonColor = keyboardButtonColor,
                    textColor = keyboardTextColor,
                    onInput = { key -> onKeyClick(key) },
                    onActionInput = { key -> onActionClick(key) }
                )
            }
            else -> {
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    /**
                     * 候補/数字バー
                     * */
                    Box(modifier = Modifier.fillMaxWidth().height((keyboardHeight * 0.8).dp)) {
                        if (uiState.inputMode == InputMode.HALF_ASCII || uiState.inputMode == InputMode.FULL_ASCII) {
                            Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                                KeyboardMap.rows[0].forEach { keyId ->
                                    val def = KeyboardMap.keyDefinitions[keyId] ?: KeyboardMap.KeyDef(keyId)
                                    SkkKey(
                                        mainText = def.main,
                                        modifier = Modifier.weight(1f),
                                        keyColor = keyboardBackgroundColor,
                                        textColor = keyboardTextColor,
                                        keyboardHeight = keyboardHeight,
                                        onClick = { onKeyClick(keyId) }
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
                         * */
                        flickLayout.forEach { rowKeys ->
                            Row(modifier = Modifier.fillMaxWidth().height(keyboardHeight.dp)) {
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
                                                    SkkState.NORMAL -> R.drawable.lucide_space
                                                    SkkState.MIDASHI -> R.drawable.lucide_square_dashed
                                                    SkkState.OKURIGANA -> R.drawable.lucide_square_asterisk
                                                    SkkState.HENKAN -> R.drawable.lucide_square_library
                                                    else -> R.drawable.lucide_space
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
                                        if (config.action == KeyboardAction.Dakuten && uiState.secondChar.isEmpty() && uiState.firstChar.isEmpty()) {
                                            actionText = "_A"
                                        }

                                        SkkKey(
                                            mainText = actionText,
                                            modifier = Modifier.weight(1f),
                                            keyColor = actionColor,
                                            textColor = if (actionColor == keyboardActionColor) keyboardActionTextColor else keyboardTextColor,
                                            iconResId = actionIcon,
                                            keyboardHeight = keyboardHeight,
                                            onClick = { onActionClick(config.action) }
                                        )
                                    } else {
                                        /* フリックキー (actionがnullの場合) */
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
                         * q,w,e,r,t,y,u,i,o,p
                         * */
                        Row(modifier = Modifier.fillMaxWidth().height(keyboardHeight.dp)) {
                            KeyboardMap.rows[1].forEach { keyId ->
                                val def =
                                    KeyboardMap.keyDefinitions[keyId] ?: KeyboardMap.KeyDef(keyId)

                                SkkKey(
                                    mainText = if (isShifted) def.main.uppercase() else def.main,
                                    flickText = if (isShifted) def.shiftFlick else def.flick,
                                    modifier = Modifier.weight(1f),
                                    keyColor = keyboardButtonColor,
                                    textColor = keyboardTextColor,
                                    flickColor = keyboardFlickColor,
                                    keyboardHeight = keyboardHeight,
                                    onFlick = { onKeyClick(if (isShifted) def.shiftFlick else def.flick) },
                                    onClick = { onKeyClick(keyId) }
                                )
                            }
                        }

                        /**
                         * a,s,d,f,g,h,j,k,l
                         * */
                        Row(modifier = Modifier.fillMaxWidth().height(keyboardHeight.dp)) {
                            Spacer(modifier = Modifier.weight(0.5f)) // 端の余白

                            KeyboardMap.rows[2].forEach { keyId ->
                                val def =
                                    KeyboardMap.keyDefinitions[keyId] ?: KeyboardMap.KeyDef(keyId)

                                SkkKey(
                                    mainText = if (isShifted) def.main.uppercase() else def.main,
                                    flickText = if (isShifted) def.shiftFlick else def.flick,
                                    modifier = Modifier.weight(1f),
                                    keyColor = keyboardButtonColor,
                                    textColor = keyboardTextColor,
                                    flickColor = keyboardFlickColor,
                                    keyboardHeight = keyboardHeight,
                                    onFlick = { onKeyClick(if (isShifted) def.shiftFlick else def.flick) },
                                    onClick = { onKeyClick(keyId) }
                                )
                            }

                            Spacer(modifier = Modifier.weight(0.5f)) // 端の余白
                        }

                        /**
                         * shift, z,x,c,v,b,n,m, back space
                         * */
                        Row(modifier = Modifier.fillMaxWidth().height(keyboardHeight.dp)) {
                            // Shiftキー
                            val shiftText = when (uiState.shiftState) {
                                ShiftState.LOWERCASE -> "Shift"
                                ShiftState.SHIFTED -> "Shift"
                                ShiftState.CAPS_LOCK -> "Caps\nLock"
                            }
                            SkkKey(
                                mainText = shiftText,
                                textSize = 18f,
                                modifier = Modifier.weight(1.5f),
                                keyColor = if (uiState.shiftState == ShiftState.LOWERCASE) keyboardBackgroundColor else keyboardActionColor,
                                textColor = if (uiState.shiftState == ShiftState.LOWERCASE) keyboardTextColor else keyboardActionTextColor,
                                keyboardHeight = keyboardHeight,
                            ) { onActionClick(KeyboardAction.Shift) }

                            // z, x, c...
                            KeyboardMap.rows[3].forEach { keyId ->
                                val def =
                                    KeyboardMap.keyDefinitions[keyId] ?: KeyboardMap.KeyDef(keyId)

                                SkkKey(
                                    mainText = if (isShifted) def.main.uppercase() else def.main,
                                    flickText = if (isShifted) def.shiftFlick else def.flick,
                                    modifier = Modifier.weight(1f),
                                    keyColor = keyboardButtonColor,
                                    textColor = keyboardTextColor,
                                    flickColor = keyboardFlickColor,
                                    keyboardHeight = keyboardHeight,
                                    onFlick = { onKeyClick(if (isShifted) def.shiftFlick else def.flick) },
                                    onClick = { onKeyClick(keyId) }
                                )
                            }

                            // Backspaceキー
                            SkkKey(
                                mainText = "BS",
                                textSize = 20f,
                                modifier = Modifier.weight(1.5f),
                                keyColor = keyboardBackgroundColor,
                                textColor = keyboardTextColor,
                                keyboardHeight = keyboardHeight,
                            ) { onActionClick(KeyboardAction.Backspace) }
                        }

                        /**
                         * Ctrl, space, Enter
                         * */
                        Row(
                            modifier = Modifier
                                .fillMaxWidth().height(keyboardHeight.dp)
                        ) {
                            // Ctrlキー
                            SkkKey(
                                mainText = "Ctrl",
                                modifier = Modifier.weight(2.5f),
                                keyColor = if (!uiState.isCtrlPressed) keyboardBackgroundColor else keyboardActionColor,
                                textColor = if (!uiState.isCtrlPressed) keyboardTextColor else keyboardActionTextColor,
                                keyboardHeight = keyboardHeight,
                            ) { onActionClick(KeyboardAction.Ctrl) }

                            // スペースキー (現在の入力モードを表示)
                            val jisyoKey =
                                if (uiState.tourokuFlag.isNotEmpty()) "辞書登録 / " else ""
                            val spaceText = when (uiState.inputMode) {
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
                            SkkKey(
                                mainText = spaceText,
                                textSize = 18f,
                                modifier = Modifier.weight(5f),
                                keyColor = keyboardButtonColor,
                                textColor = keyboardActionColor,
                                keyboardHeight = keyboardHeight,
                            ) { onActionClick(KeyboardAction.Space) }

                            // Enterキー
                            SkkKey(
                                mainText = "Enter",
                                modifier = Modifier.weight(2.5f),
                                keyColor = keyboardActionColor,
                                textColor = keyboardActionTextColor,
                                keyboardHeight = keyboardHeight,
                            ) { onActionClick(KeyboardAction.Enter) }
                        }
                    }
                }
            }
        }
    }
}