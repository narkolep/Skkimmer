package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.keyboard.handlers.handleCtrl
import com.narkolep.skkimmer.keyboard.handlers.handleKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

class KeyProcessor(
    internal val stateFlow: MutableStateFlow<KeyboardState>,
    internal val keyboardService: KeyboardService,
    internal val outputManager: OutputManager,
    internal val inputCommitter: InputCommitter
) {

    fun handle(key: String) {
        val capturedOldState = stateFlow.getAndUpdate { current ->
            current.copy(
                shiftState =
                    if (current.shiftState == ShiftState.SHIFTED) ShiftState.LOWERCASE
                    else current.shiftState,
                isCtrlPressed = false
            )
        }

        // CTRL押下時のショートカット処理
        val newKey = handleCtrl(
            key,
            capturedOldState,
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
        if (handleKey(capturedOldState, result)) return

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

        when (state.currentSkkState.skkMode) {
            SkkMode.NORMAL -> {
                stateFlow.update {
                    it.changeMode(SkkMode.MIDASHI)
                }
            }
            SkkMode.MIDASHI -> {
                /* key が数字である場合は何もしない */
                if (key.isNumber()) return
                if (state.currentSkkState.midashiText.isNotEmpty()) {
                    stateFlow.update {
                        it.changeMode(SkkMode.OKURIGANA)
                    }
                }
            }
            SkkMode.HENKAN -> {
                outputManager.commit()
                stateFlow.update {
                    it.changeMode(SkkMode.MIDASHI)
                }
            }
            else -> { /* OKURIGANA, ABBREV は何もしない */ }
        }
    }
}