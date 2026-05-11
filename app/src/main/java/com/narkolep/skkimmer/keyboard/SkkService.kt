package com.narkolep.skkimmer.keyboard

import android.R as R1
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.narkolep.skkimmer.data.EmojiManager
import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import com.narkolep.skkimmer.data.SkkDictionaryManager
import com.narkolep.skkimmer.keyboard.mappings.FlickKanaMap
import com.narkolep.skkimmer.keyboard.ui.KeyboardLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.contains

class SkkService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val uiStateFlow = MutableStateFlow(SkkUIState())
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store
    private lateinit var dictionaryManager: SkkDictionaryManager
    private var emojiCategories: List<EmojiManager.Category> = emptyList()

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        dictionaryManager = SkkDictionaryManager(this)

        lifecycleScope.launch {
            val parsedList = withContext(Dispatchers.IO) {
                val jsonString =
                    assets.open("all-emoji.json").bufferedReader().use { it.readText() }
                EmojiManager(this@SkkService).loadEmojis(jsonString)
            }
            emojiCategories = parsedList
        }
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this)

        val decorView = window?.window?.decorView
        if (decorView != null) {
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }

        composeView.apply {
            setViewTreeLifecycleOwner(this@SkkService)
            setViewTreeViewModelStoreOwner(this@SkkService)
            setViewTreeSavedStateRegistryOwner(this@SkkService)

            setContent {
                val uiState by uiStateFlow.collectAsState()

                KeyboardLayout(
                    uiState = uiState,
                    categories = emojiCategories,
                    onKeyClick = { keyId -> handleKeyClick(keyId) },
                    onActionClick = { action -> handleActionClick(action) }
                )
            }
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        return composeView
    }

    private var currentEditorInfo: EditorInfo? = null

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        currentEditorInfo = attribute
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)

        // 自動判定したモードを取得
        val autoMode = determineInputMode(editorInfo)

        // UIステートの入力モードを更新
        updateUiState { current ->
            current.copy(
                inputMode = autoMode,
                skkState = SkkState.NORMAL,
                composingText = "",
                isFlick = (autoMode == InputMode.HIRAGANA)
            )
        }
    }

    /**
     * EditorInfoから適切なInputModeを決定する関数
     */
    private fun determineInputMode(editorInfo: EditorInfo?): InputMode {
        if (editorInfo == null) return InputMode.HIRAGANA

        val inputType = editorInfo.inputType
        val classType = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION

        return when (classType) {
            // --- 1. 数字のみの入力欄（テンキーを出すべき状態） ---
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_PHONE,
            InputType.TYPE_CLASS_DATETIME -> {
                // ※テンキー用のモードが別にある場合はそちらを指定してください
                InputMode.NUMERIC // (例: テンキーモード)
            }

            // --- 2. テキストの入力欄 ---
            InputType.TYPE_CLASS_TEXT -> {
                when (variation) {
                    // パスワード、メールアドレス、URLなどは最初から半角英数にする
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
                    InputType.TYPE_TEXT_VARIATION_URI -> {
                        InputMode.HALF_ASCII
                    }
                    // それ以外の通常のテキスト欄はひらがな（日本語入力）
                    else -> {
                        InputMode.HIRAGANA
                    }
                }
            }

            // --- 3. その他（デフォルト） ---
            else -> InputMode.HIRAGANA
        }
    }

    /**
     * キー入力の処理
     **/
    private fun handleKeyClick(keyId: String) {
        /* stateにこの時点での状態を保存 */
        val state = uiStateFlow.value
        val isUpper = state.shiftState != ShiftState.LOWERCASE
        val inputChar = if (isUpper) keyId.uppercase() else keyId

        /* Shiftキーを元に戻す */
        if (state.shiftState == ShiftState.SHIFTED) {
            updateUiState { it.copy(shiftState = ShiftState.LOWERCASE) }
        }

        /* Ctrlキーを元に戻す */
        updateUiState { it.copy(isCtrlPressed = false) }

        /* Ctrl + keyId (早期 return) */
        if (state.isCtrlPressed) {
            when (keyId) {
                "j" -> {
                    /* Japanese input */
                    updateUiState { it.copy(inputMode = InputMode.HIRAGANA) }
                    return
                }
                "e" -> {
                    /* Emoji */
                    updateUiState { it.copy(inputMode = InputMode.EMOJI) }
                    return
                }
                "k" -> {
                    /* switching Keyboards */
                    updateUiState { it.copy(
                        isFlick = !state.isFlick,
                        inputMode = InputMode.HIRAGANA
                    ) }
                    return
                }
                "n" -> {
                    /* Numeric keypad */
                    updateUiState { it.copy(inputMode = InputMode.NUMERIC) }
                    return
                }
                "a" -> {
                    currentInputConnection.performContextMenuAction(R1.id.selectAll)
                    return
                }
                "x" -> {
                    currentInputConnection.performContextMenuAction(R1.id.cut)
                    return
                }
                "c" -> {
                    currentInputConnection.performContextMenuAction(R1.id.copy)
                    return
                }
                "v" -> {
                    currentInputConnection.performContextMenuAction(R1.id.paste)
                    return
                }
            }
        }

        /* 英数/abbrevモードのときは直接入力 (return) */
        when (state.inputMode) {
            InputMode.HALF_ASCII, InputMode.NUMERIC -> {
                currentInputConnection?.commitText(inputChar, 1)
                return
            }
            InputMode.FULL_ASCII -> {
                val fullChar =
                    if (inputChar.first().code in 0x21..0x7E) (inputChar.first().code + 0xFEE0).toChar().toString()
                    else inputChar
                currentInputConnection?.commitText(fullChar, 1)
                return
            }
            else -> {
                if (state.skkState == SkkState.ABBREV) {
                    updateUiState { it.copy(midashiText = state.midashiText + inputChar) }
                    updateComposing()
                    return
                }
            }
        }

        /* これ以降、日本語入力の処理 */
        val composingNow = state.composingText + keyId
        var composingNext: String
        val outputChar: String
        var okuriganaFlag: String
        var isIgnoreChar = false // 「ん」と「っ」

        /* 入力処理に使う判定 */
        val vowels = setOf('a','i','u','e','o','y') // 母音 + y
        val isAlphabet = keyId.matches(Regex("^[a-z0-9]+$")) // アルファベットか判定する

        /* 連結処理 */
        if (KanaMap.romajiToKana.containsKey(composingNow)) {
            /* 先頭から完全一致の場合 */
            composingNext = ""
            outputChar = KanaMap.getOutputChar(KanaMap.romajiToKana[composingNow]!!, state.inputMode) // 仮名を取得
            okuriganaFlag = composingNow.firstOrNull().toString()
            /* xtuのとき等、okuriTriggerをxにしない */
            if (okuriganaFlag == "x") {
                okuriganaFlag = composingNow.getOrElse(1) { 'x' }.toString()
                isIgnoreChar = true
            }
            /* 母音と子音を記録 */
            updateUiState { it.copy(
                secondChar = if (isAlphabet) keyId else ""
            ) }
            if (state.composingText.isEmpty()) updateUiState { it.copy(firstChar = "") }
        } else if (keyId.isNotEmpty() && KanaMap.romajiToKana.keys.any { it.startsWith(composingNow) }) {
            /* 先頭から部分一致の場合 */
            composingNext = composingNow
            outputChar = "" // 何も確定しない
            okuriganaFlag = composingNow.firstOrNull().toString()
            /* 母音と子音を記録 */
            updateUiState { it.copy(
                firstChar = keyId,
                secondChar = ""
            ) }
        } else if (isAlphabet && state.composingText == keyId) {
            /* 促音 */
            composingNext = keyId
            outputChar = KanaMap.getOutputChar(KanaMap.KanaDefinition("っ", "ッ", "ｯ"), state.inputMode)
            okuriganaFlag = "t"
            isIgnoreChar = true
        } else if (state.composingText == "n" && keyId.firstOrNull() !in vowels) {
            /* 撥音 */
            composingNext = keyId
            outputChar = KanaMap.getOutputChar(KanaMap.KanaDefinition("ん", "ン", "ﾝ"), state.inputMode)
            okuriganaFlag = "n"
            isIgnoreChar = true
        } else if (KanaMap.romajiToKana.containsKey(keyId)) {
            /* composingNowが一致しない、かつkeyIdが完全一致の場合 */
            composingNext = ""
            outputChar = KanaMap.getOutputChar(KanaMap.romajiToKana[keyId]!!, state.inputMode)
            okuriganaFlag = keyId
        } else if (keyId.isNotEmpty() && KanaMap.romajiToKana.keys.any { it.startsWith(keyId) }) {
            /* composingNowが一致しない、かつkeyIdが部分一致の場合 */
            composingNext = keyId
            outputChar = "" // 何も確定しない
            okuriganaFlag = keyId
        } else {
            /* 何にも一致しなかった場合 */
            composingNext = ""
            outputChar = composingNow
            okuriganaFlag = ""
        }

        /* キーボードショートカット */
        val keyChar = if (composingNext.isNotEmpty()) composingNext.last() else outputChar.lastOrNull()
        when (keyChar) {
            'x' -> {
                if (uiStateFlow.value.skkState == SkkState.HENKAN) {
                    var index = uiStateFlow.value.selectedIndex
                    if (index > 0) index -= 1
                    updateUiState { it.copy(selectedIndex = index) }
                    updateComposing()
                    return
                }
            }
            'q' -> {
                val beforeConvert = if (isIgnoreChar) state.midashiText + outputChar else state.midashiText
                val afterConvert: String

                if (state.isCtrlPressed) {
                    if (state.skkState == SkkState.MIDASHI) {
                        afterConvert = KanaMap.convertString(beforeConvert, KanaMap.hiraToHalfMap)
                        updateUiState { it.copy(
                            midashiText = afterConvert,
                            composingText = ""
                        ) }
                    } else {
                        if (state.inputMode == InputMode.HALF_KATAKANA) {
                            updateUiState { it.copy(inputMode = InputMode.HIRAGANA) }
                        } else {
                            updateUiState { it.copy(inputMode = InputMode.HALF_KATAKANA) }
                        }
                    }
                } else {
                    if (state.skkState == SkkState.MIDASHI) {
                        afterConvert = if (state.inputMode == InputMode.KATAKANA) {
                            KanaMap.convertString(beforeConvert, KanaMap.kataToHiraMap)
                        } else {
                            KanaMap.convertString(beforeConvert, KanaMap.hiraToKataMap)
                        }
                        updateUiState { it.copy(
                            midashiText = afterConvert,
                            composingText = ""
                        ) }
                    } else {
                        if (state.inputMode == InputMode.KATAKANA) {
                            updateUiState { it.copy(inputMode = InputMode.HIRAGANA) }
                        } else {
                            updateUiState { it.copy(inputMode = InputMode.KATAKANA) }
                        }
                    }
                }
                commitBufferWord()
                return
            }
            'l' -> {
                if (isUpper) {
                    updateUiState { it.copy(inputMode = InputMode.FULL_ASCII) }
                } else {
                    updateUiState { it.copy(inputMode = InputMode.HALF_ASCII) }
                }
                commitBufferWord()
                return
            }
            '/' -> {
                if (state.skkState == SkkState.NORMAL) {
                    updateUiState { it.copy(
                        skkState = SkkState.ABBREV,
                        composingText = " "
                    ) }
                    updateComposing()
                    return
                }
            }
        }

        /* Shiftキーによるモード遷移 */
        if ((state.inputMode == InputMode.HIRAGANA || state.inputMode == InputMode.KATAKANA) && isUpper && isAlphabet) {
            if (state.skkState == SkkState.NORMAL) {
                updateUiState { it.copy(skkState = SkkState.MIDASHI) }
            }
            if (state.skkState == SkkState.MIDASHI && state.midashiText.isNotEmpty()) {
                updateUiState { it.copy(
                    skkState = SkkState.OKURIGANA,
                    okuriganaTrigger = okuriganaFlag
                ) }
            }
            if (state.skkState == SkkState.HENKAN) {
                commitBufferWord()
                updateUiState { it.copy(
                    skkState = SkkState.MIDASHI,
                    composingText = composingNext
                ) }
            }
        }

        /* 文字の出力/変換処理 */
        updateUiState { it.copy(composingText = composingNext) }
        when (uiStateFlow.value.skkState) {
            SkkState.NORMAL -> {
                /* 確定入力 */
                currentInputConnection?.commitText(outputChar, 1)
            }
            SkkState.MIDASHI -> {
                /* midashiTextを出力 */
                updateUiState { it.copy(
                    midashiText = uiStateFlow.value.midashiText + outputChar
                ) }
            }
            SkkState.OKURIGANA -> {
                if (outputChar.isEmpty() || isIgnoreChar) {
                    /* outputText == "ん" もしくは 未確定 */
                    updateUiState { it.copy(
                        midashiText = uiStateFlow.value.midashiText + if (state.skkState == SkkState.MIDASHI) outputChar else "",
                        okuriganaText = uiStateFlow.value.okuriganaText + if (state.skkState == SkkState.OKURIGANA) outputChar else "",
                        okuriganaTrigger = okuriganaFlag // フラグを更新
                    ) }
                } else {
                    /* 仮名(ん,っ,以外)が出力されたとき、候補を検索する */
                    if (state.okuriganaText.firstOrNull() == 'ん' || state.okuriganaText.firstOrNull() == 'ン') okuriganaFlag = "n"
                    if (state.okuriganaText.firstOrNull() == 'っ' || state.okuriganaText.firstOrNull() == 'ッ') okuriganaFlag = "t"

                    updateUiState { it.copy(
                        okuriganaText = uiStateFlow.value.okuriganaText + outputChar,
                        okuriganaTrigger = okuriganaFlag
                    ) }

                    val keyText = uiStateFlow.value.midashiText + uiStateFlow.value.okuriganaTrigger
                    CoroutineScope(Dispatchers.Main).launch {
                        val candidatesList = dictionaryManager.getCandidates(keyText)
                        if (candidatesList.isNotEmpty()) {
                            updateUiState { it.copy(
                                skkState = SkkState.HENKAN,
                                candidates = candidatesList,
                                selectedIndex = 0
                            ) }
                        } else {
                            /* 候補が無ければ、辞書登録モードに移行 */
                            updateUiState { it.copy(
                                oldOkuriganaTrigger = uiStateFlow.value.okuriganaTrigger,
                                tourokuText = keyText,
                                tourokuFlag = "[登録]$keyText:"
                            ) }
                            resetSkkState()
                        }
                        updateComposing()
                    }
                }
            }
            SkkState.HENKAN -> {
                /* 変換中に文字入力があったら、確定する */
                updateUiState { it.copy(okuriganaText = uiStateFlow.value.okuriganaText + outputChar) }
                commitBufferWord()
                updateUiState { it.copy(composingText = composingNext) }
            }
            else -> {}
        }
        updateComposing()
    }

    /**
     * モードに合わせて setComposingText() を実行する関数
     * */
    private fun updateComposing() {
        val state = uiStateFlow.value

        val displayComposing = when (state.skkState) {
            SkkState.NORMAL -> {
                if (state.composingText.isEmpty() && state.tourokuFlag.isEmpty()) {
                    currentInputConnection?.commitText("", 1)
                    return
                }
                state.tourokuFlag + state.composingText
            }
            SkkState.MIDASHI -> {
                state.tourokuFlag + "▽" + state.midashiText + state.composingText
            }
            SkkState.OKURIGANA -> {
                state.tourokuFlag + "▽" + state.midashiText + "*" + state.okuriganaText + state.composingText
            }
            SkkState.HENKAN -> {
                val displayOkuri = if (state.okuriganaText.isNotEmpty()) "*" + state.okuriganaText + state.composingText else ""
                val candidates = state.candidates[state.selectedIndex].split(";")[0]
                state.tourokuFlag + "▼" + candidates + displayOkuri
            }
            SkkState.ABBREV -> {
                state.tourokuFlag + state.midashiText.ifEmpty { state.composingText }
            }
        }
        currentInputConnection?.setComposingText(displayComposing, 1)
    }

    /**
     * 文字列の確定を行う関数
     * */
    private fun commitBufferWord() {
        val state = uiStateFlow.value
        var textToCommit: Any

        when (state.skkState) {
            SkkState.NORMAL -> {
                textToCommit = state.composingText
            }
            SkkState.MIDASHI -> {
                textToCommit = state.midashiText + state.composingText
            }
            SkkState.OKURIGANA -> {
                textToCommit = if (state.tourokuFlag.isNotEmpty()) state.midashiText + "*" + state.composingText else state.midashiText + state.composingText
            }
            SkkState.HENKAN -> {
                val word = state.candidates[state.selectedIndex].split(";")[0]
                val okuri = if (state.tourokuFlag.isNotEmpty() && state.okuriganaText.isNotEmpty()) "*" + state.okuriganaText else state.okuriganaText
                textToCommit = word + okuri

                // 履歴を記録(汎用)
                val learnText = state.midashiText + state.okuriganaTrigger
                CoroutineScope(Dispatchers.IO).launch {
                    dictionaryManager.learnWord(learnText, state.candidates[state.selectedIndex], true)
                }
            }
            SkkState.ABBREV -> {
                textToCommit = state.midashiText
            }
        }

        resetSkkState()

        /* リセットした後、フラグを更新 */
        if (state.tourokuFlag.isEmpty()) {
            if (textToCommit.isNotEmpty()) currentInputConnection?.commitText(textToCommit, 1)
        } else {
            updateUiState { it.copy(tourokuFlag = state.tourokuFlag + textToCommit) }
            updateComposing()
        }
    }

    /**
     * 各modifier keyが押されたときの動作
     * */
    private fun handleActionClick(action: String) {
        val state = uiStateFlow.value

        when {
            action == "SHIFT" -> {
                val now = System.currentTimeMillis().toInt()
                val timing = now - state.lastShiftPressTime < 500
                val nextShift = when (state.shiftState) {
                    ShiftState.LOWERCASE -> ShiftState.SHIFTED
                    ShiftState.SHIFTED -> {
                        if (timing && (state.inputMode == InputMode.HALF_ASCII || state.inputMode == InputMode.FULL_ASCII))
                            ShiftState.CAPS_LOCK
                        else
                            ShiftState.LOWERCASE
                    }
                    ShiftState.CAPS_LOCK -> ShiftState.LOWERCASE
                }
                updateUiState { it.copy(
                    lastShiftPressTime = now,
                    shiftState = nextShift
                ) }
            }
            action == "CTRL" -> {
                updateUiState { it.copy(isCtrlPressed = !state.isCtrlPressed) }
            }
            action == "SPACE" -> {
                when (state.skkState) {
                    SkkState.NORMAL -> {
                        updateUiState { it.copy(
                            firstChar = "",
                            secondChar = ""
                        ) }
                        if (state.tourokuFlag.isNotEmpty()) {
                            updateUiState { it.copy(tourokuFlag = state.tourokuFlag + " ") }
                            updateComposing()
                        } else {
                            currentInputConnection?.commitText(" ", 1)
                        }
                    }
                    SkkState.MIDASHI -> {
                        /* 連結処理をしてから変換 */
                        handleKeyClick("")

                        CoroutineScope(Dispatchers.Main).launch {
                            val candidatesList = dictionaryManager.getCandidates(state.midashiText)
                            if (candidatesList.isNotEmpty()) {
                                updateUiState { it.copy(
                                    skkState = SkkState.HENKAN,
                                    candidates = candidatesList,
                                    selectedIndex = 0
                                ) }
                                updateComposing()
                            } else if (state.tourokuFlag.isEmpty()) {
                                updateUiState { it.copy(
                                    tourokuText = state.midashiText,
                                    tourokuFlag = "[登録]" + state.midashiText + ":"
                                ) }
                                resetSkkState()
                                updateComposing()
                            }
                        }
                    }
                    SkkState.OKURIGANA -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            val candidatesList = dictionaryManager.getCandidates(state.midashiText)
                            if (candidatesList.isNotEmpty()) {
                                updateUiState { it.copy(
                                    skkState = SkkState.HENKAN,
                                    candidates = candidatesList,
                                    selectedIndex = 0
                                ) }
                                updateComposing()
                            } else if (state.tourokuFlag.isEmpty()) {
                                updateUiState { it.copy(
                                    tourokuText = state.midashiText,
                                    tourokuFlag = "[登録]" + state.midashiText + ":"
                                ) }
                                resetSkkState()
                                updateComposing()
                            }
                        }
                    }
                    SkkState.HENKAN -> {
                        if (state.candidates.isNotEmpty()) {
                            if (state.selectedIndex < state.candidates.size - 1) {
                                updateUiState { it.copy(selectedIndex = state.selectedIndex + 1) }
                                updateComposing()
                            } else if (state.tourokuFlag.isEmpty()) {
                                updateUiState { it.copy(
                                    tourokuText = state.midashiText + state.okuriganaTrigger,
                                    tourokuFlag = "[登録]" + state.midashiText + state.okuriganaTrigger + ":"
                                ) }
                                resetSkkState()
                                updateComposing()
                            }
                        }
                    }
                    SkkState.ABBREV -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            val candidatesList = dictionaryManager.getCandidates(state.midashiText)
                            if (candidatesList.isNotEmpty()) {
                                updateUiState { it.copy(
                                    skkState = SkkState.HENKAN,
                                    candidates = candidatesList,
                                    selectedIndex = 0
                                ) }
                                updateComposing()
                            } else if (state.tourokuFlag.isEmpty()) {
                                updateUiState { it.copy(
                                    tourokuText = state.midashiText,
                                    tourokuFlag = "[登録]" + state.midashiText + ":"
                                ) }
                                resetSkkState()
                                updateComposing()
                            }
                        }
                    }
                }
            }
            action == "BACKSPACE" -> {
                updateUiState { it.copy(
                    firstChar = "",
                    secondChar = ""
                ) }
                /* 範囲選択されているときは空文字を出力 */
                if (!currentInputConnection.getSelectedText(0).isNullOrEmpty()) {
                    currentInputConnection.commitText("", 1)
                } else when (state.skkState) {
                    SkkState.NORMAL -> {
                        if (state.composingText.isNotEmpty()) {
                            updateUiState { it.copy(composingText = state.composingText.dropLast(1)) }
                        } else if (state.tourokuFlag.isNotEmpty()) {
                            if (state.tourokuFlag.substringAfter(":").isNotEmpty()) {
                                updateUiState { it.copy(tourokuFlag = state.tourokuFlag.dropLast(1)) }
                            } else if (state.oldOkuriganaTrigger.isNotEmpty()) {
                                updateUiState { it.copy(
                                    midashiText = state.tourokuText.removeSuffix(state.oldOkuriganaTrigger),
                                    composingText = state.oldOkuriganaTrigger,
                                    okuriganaTrigger = state.oldOkuriganaTrigger,
                                    oldOkuriganaTrigger = "",
                                    skkState = SkkState.OKURIGANA,
                                    tourokuText = "",
                                    tourokuFlag = ""
                                ) }
                            } else {
                                updateUiState { it.copy(
                                    midashiText = state.tourokuText,
                                    skkState = SkkState.MIDASHI,
                                    tourokuText = "",
                                    tourokuFlag = ""
                                ) }
                            }
                        } else {
                            currentInputConnection?.deleteSurroundingText(1, 0)
                        }
                    }
                    SkkState.MIDASHI -> {
                        if (state.composingText.isNotEmpty()) {
                            val composing = state.composingText.dropLast(1)
                            updateUiState { it.copy(composingText = composing) }
                        } else if (state.midashiText.isNotEmpty()) {
                            val midashi = state.midashiText.dropLast(1)
                            updateUiState { it.copy(midashiText = midashi) }
                        } else {
                            resetSkkState()
                        }
                    }
                    SkkState.OKURIGANA -> {
                        if (state.composingText.isNotEmpty()) {
                            val composing = state.composingText.dropLast(1)
                            updateUiState { it.copy(composingText = composing) }
                        } else if (state.okuriganaText.isNotEmpty()) {
                            val okuri = state.okuriganaText.dropLast(1)
                            updateUiState { it.copy(okuriganaText = okuri) }
                        } else {
                            updateUiState { it.copy(
                                skkState = SkkState.MIDASHI,
                                okuriganaTrigger = "",
                                candidates = emptyList(),
                                selectedIndex = -1
                            ) }
                        }
                    }
                    SkkState.HENKAN -> {
                        if (state.okuriganaText.isNotEmpty()) {
                            val okuri = state.okuriganaText.dropLast(1)
                            updateUiState { it.copy(
                                skkState = SkkState.OKURIGANA,
                                okuriganaText = okuri,
                                composingText = "",
                                candidates = emptyList(),
                                selectedIndex = -1
                            ) }
                        } else {
                            updateUiState { it.copy(
                                skkState = SkkState.MIDASHI,
                                candidates = emptyList(),
                                selectedIndex = -1
                            ) }
                        }
                    }
                    SkkState.ABBREV -> {
                        if (state.midashiText.isNotEmpty()) {
                            val midashi = state.midashiText.dropLast(1)
                            updateUiState { it.copy(midashiText = midashi) }
                        } else if (state.composingText.isNotEmpty()) {
                            val composing = state.composingText.dropLast(1)
                            updateUiState { it.copy(composingText = composing) }
                            if (composing.isEmpty()) {
                                resetSkkState()
                            }
                        }
                    }
                }
                updateComposing()
            }
            action == "ENTER" -> {
                updateUiState { it.copy(
                    firstChar = "",
                    secondChar = ""
                ) }
                if (state.skkState == SkkState.NORMAL) {
                    if (state.tourokuFlag.isNotEmpty()) {
                        val textToCommit = state.tourokuFlag.split(":")[1]
                        currentInputConnection?.commitText(textToCommit.replace("*", ""), 1)

                        /* ユーザー辞書として登録 */
                        val learnText = textToCommit.split("*")[0]
                        if (learnText.isNotEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                dictionaryManager.learnWord(
                                    state.tourokuText,
                                    learnText,
                                    false
                                )
                                updateUiState { it.copy(
                                    tourokuText = "",
                                    tourokuFlag = ""
                                ) }
                            }
                        }
                        resetSkkState()
                    } else if (state.composingText.isEmpty()) {
                        val editorInfo = currentEditorInfo
                        val action = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
                        val inputType = editorInfo?.inputType ?: 0
                        val imeOptions = editorInfo?.imeOptions ?: 0

                        val isMultiLine =
                            (inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
                        var noEnterAction = (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
                        if (state.shiftState != ShiftState.LOWERCASE) {
                            noEnterAction = true
                            if (state.shiftState != ShiftState.SHIFTED) updateUiState { it.copy(shiftState = ShiftState.LOWERCASE) }
                        }

                        if (isMultiLine || noEnterAction) {
                            // 改行
                            currentInputConnection.commitText("\n", 1)
                        } else if (action != null && action != EditorInfo.IME_ACTION_NONE) {
                            // アクション実行
                            currentInputConnection.performEditorAction(action)
                        } else {
                            // fallback (改行)
                            currentInputConnection.commitText("\n", 1)
                        }
                    } else {
                        commitBufferWord()
                    }
                }
                else {
                    commitBufferWord()
                }
            }
            action == "TOGGLE_EMOJI" -> {
                updateUiState { it.copy(inputMode = InputMode.HIRAGANA) }
            }
            action == "TOGGLE_NUMERIC" -> {
                updateUiState { it.copy(inputMode = InputMode.HIRAGANA) }
            }
            action.startsWith("CANDIDATE_INDEX:") -> {
                val index = action.removePrefix("CANDIDATE_INDEX:").toInt()
                updateUiState { it.copy(selectedIndex = index) }
                commitBufferWord()
            }
            action == "LEFT" -> {
                updateUiState { it.copy(
                    firstChar = "",
                    secondChar = ""
                ) }
                moveCursor(-1)
            }
            action == "RIGHT" -> {
                updateUiState { it.copy(
                    firstChar = "",
                    secondChar = ""
                ) }
                moveCursor(1)
            }
            action == "DAKUTEN" -> {
                if (state.secondChar.isNotEmpty()) {
                    val match = FlickKanaMap.flickConvert.find { it.consonantBefore == state.firstChar }

                    if (state.tourokuFlag.isNotEmpty()) {
                        /* 登録モードから抜ける */
                        handleActionClick("BACKSPACE")
                    }

                    if (state.inputMode != InputMode.HIRAGANA && state.firstChar == "x" && state.secondChar == "u") {
                        // ウ -> ヴ
                        handleActionClick("BACKSPACE")
                        handleKeyClick("v")
                        handleKeyClick(state.secondChar)
                    } else if (state.firstChar == "t" && state.secondChar == "u") {
                        // つ -> っ
                        handleActionClick("BACKSPACE")
                        handleKeyClick("xt")
                        handleKeyClick(state.secondChar)
                    } else if (match != null) {
                        // それ以外
                        handleActionClick("BACKSPACE")
                        if (match.backspace == 2 && state.inputMode == InputMode.HALF_KATAKANA) {
                            // 2文字消す場合
                            handleActionClick("BACKSPACE")
                        }
                        handleKeyClick(match.consonantAfter)
                        handleKeyClick(state.secondChar)
                    }
                } else if (state.firstChar.isEmpty()) {
                    updateUiState { it.copy(
                        inputMode = InputMode.HALF_ASCII,
                        isFlick = false
                    ) }
                }
            }
        }
    }

    private fun moveCursor(offset: Int) {
        val ic = currentInputConnection ?: return
        val extracted = ic.getExtractedText(ExtractedTextRequest(), 0) ?: return
        val textLength = extracted.text?.length ?: 0
        val current = extracted.selectionStart
        val newPos = (current + offset).coerceIn(0, textLength)

        ic.setSelection(newPos, newPos)
    }

    private fun resetSkkState() {
        updateUiState { it.copy(
            skkState = SkkState.NORMAL,
            midashiText = "",
            composingText = "",
            okuriganaText = "",
            okuriganaTrigger = "",
            candidates = emptyList(),
            selectedIndex = -1,
            secondChar = ""
        ) }
        currentInputConnection?.setComposingText("", 1)
    }

    private fun updateUiState(update: (SkkUIState) -> SkkUIState) {
        uiStateFlow.update(update)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)

        commitBufferWord()
        updateUiState { it.copy(
            tourokuText = "",
            tourokuFlag = ""
        ) }
        resetSkkState()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()

        super.onDestroy()
    }
}