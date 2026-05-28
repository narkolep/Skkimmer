package com.narkolep.skkimmer.keyboard

import android.view.inputmethod.InputConnection
import com.narkolep.skkimmer.data.DictionaryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class KeyProcessor(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    dictionaryManager: DictionaryManager,
    connectionProvider: () -> InputConnection?
) {
    private val inputCommitter = InputCommitter(connectionProvider)
    private val outputManager = OutputManager(stateFlow, inputCommitter, dictionaryManager)
    private val romajiConverter = Converter()
    private val shortcutHandler = ShortcutHandler(stateFlow, inputCommitter, outputManager)
    private val controlState = ControlState(stateFlow, outputManager)

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
        if (shortcutHandler.handleCTRL(key, state)) return

        // 英数字(直接出力)
        if (outputManager.asciiOutput(key, state)) return

        // ローマ字変換
        val result = romajiConverter.convert(
            composing = state.composingText,
            key = key,
            inputMode = state.inputMode
        )

        // ショートカット処理 その2
        if (shortcutHandler.handleKey(state, result)) return

        // shiftキーによる状態遷移
        controlState.changeState(key, state)

        // resultの出力処理
        val oldState = state
        val newState = stateFlow.value
        outputManager.kanaOutput(newState, oldState, result)
    }
}