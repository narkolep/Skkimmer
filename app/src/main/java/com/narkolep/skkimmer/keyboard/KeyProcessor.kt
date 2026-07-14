package com.narkolep.skkimmer.keyboard

import android.view.inputmethod.InputConnection
import com.narkolep.skkimmer.data.DictionaryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class KeyProcessor(
    private val stateFlow: MutableStateFlow<KeyboardState>,
    dictionaryManager: DictionaryManager,
    connectionProvider: () -> InputConnection?
) {
    private val inputCommitter = InputCommitter(connectionProvider)
    private val outputManager = OutputManager(stateFlow, inputCommitter, dictionaryManager)

    fun handle(key: String) {
        val state = stateFlow.value

        // ShiftとCtrlを元に戻す
        stateFlow.update { it.copy(
                shiftState =
                    if (it.shiftState == ShiftState.SHIFTED) ShiftState.LOWERCASE
                    else it.shiftState,
                isCtrlPressed = false
        ) }

        // ショートカット処理 その1
        if (handleCTRL(key, state, stateFlow, inputCommitter, outputManager)) return

        // 英数字(直接出力)
        if (outputManager.asciiOutput(key, state)) return

        // ローマ字変換
        val result = romajiConverter(
            composing = state.composingText,
            key = key,
            inputMode = state.inputMode
        )

        // ショートカット処理 その2
        if (handleKey(state, stateFlow, result, outputManager)) return

        // shiftキーによる状態遷移
        changeState(key, state)

        // resultの出力処理
        val oldState = state
        val newState = stateFlow.value
        outputManager.kanaOutput(newState, oldState, result)
    }

    private fun changeState(
        key: String,
        state: KeyboardState
    ) {
        if (!(state.inputMode == InputMode.HIRAGANA || state.inputMode == InputMode.KATAKANA)) return
        if (state.shiftState == ShiftState.LOWERCASE) return
        if (!key.matches(Regex("^[a-z0-9]+$"))) return

        if (state.skkState == SkkState.NORMAL) {
            stateFlow.update { it.copy(
                skkState = SkkState.MIDASHI
            ) }
        }

        if (state.skkState == SkkState.MIDASHI && state.midashiText.isNotEmpty()) {
            stateFlow.update { it.copy(
                skkState = SkkState.OKURIGANA
            ) }
        }

        if (state.skkState == SkkState.HENKAN) {
            outputManager.commit()
            stateFlow.update { it.copy(
                skkState = SkkState.MIDASHI
            ) }
        }
    }
}