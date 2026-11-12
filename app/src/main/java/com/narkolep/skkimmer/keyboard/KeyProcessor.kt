package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.keyboard.handlers.handleCTRL
import com.narkolep.skkimmer.keyboard.handlers.handleKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class KeyProcessor(
    private val stateFlow: MutableStateFlow<KeyboardState>,
    private val keyboardService: KeyboardService,
    private val outputManager: OutputManager,
    private val inputCommitter: InputCommitter
) {

    fun handle(key: String) {
        // ShiftとCtrlのリセット
        // リセット前の状態をキャプチャ
        var oldState: KeyboardState? = null
        stateFlow.update {
            oldState = it
            it.copy(
                shiftState =
                    if (it.shiftState == ShiftState.SHIFTED) ShiftState.LOWERCASE
                    else it.shiftState,
                isCtrlPressed = false
            )
        }
        val capturedOldState = oldState ?: return

        // CTRL押下時のショートカット処理
        val newKey = handleCTRL(
            key,
            capturedOldState,
            stateFlow,
            inputCommitter,
            outputManager,
            keyboardService
        ) ?: return

        // 英数字(直接出力)
        if (outputManager.asciiOutput(newKey, capturedOldState)) return

        // ローマ字変換
        val result = romajiConverter(
            composing = capturedOldState.composingText,
            key = newKey,
            inputMode = capturedOldState.inputMode
        )

        // ショートカット処理
        if (handleKey(capturedOldState, stateFlow, result, outputManager)) return

        // shiftキーによる状態遷移
        changeState(newKey, capturedOldState)

        // resultの出力処理
        val newState = stateFlow.value
        outputManager.kanaOutput(newState, capturedOldState, result)
    }

    /**
     * Shiftキーが押されていたとき、
     * 元のSkkStateから次のSkkStateを決定する
     */
    private fun changeState(
        key: String,
        state: KeyboardState
    ) {
        if (state.inputMode != InputMode.HIRAGANA && state.inputMode != InputMode.KATAKANA) return
        if (state.shiftState == ShiftState.LOWERCASE) return
        if (!key.isAlphabet() && !key.isNumber()) return

        when (state.skkState) {
            SkkState.NORMAL -> {
                stateFlow.update { it.copy(skkState = SkkState.MIDASHI) }
            }
            SkkState.MIDASHI -> {
                /* key が数字である場合は何もしない */
                if (key.isNumber()) return
                if (state.midashiText.isNotEmpty()) {
                    stateFlow.update { it.copy(skkState = SkkState.OKURIGANA) }
                }
            }
            SkkState.HENKAN -> {
                outputManager.commit()
                stateFlow.update { it.copy(skkState = SkkState.MIDASHI) }
            }
            else -> { /* OKURIGANA, ABBREV は何もしない */ }
        }
    }
}