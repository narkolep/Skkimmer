package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.keyboard.SkkConstants.FULL_SPACE
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

        // 英数字
        if (asciiOutput(newKey, capturedOldState)) return

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
        outputManager.kanaOutput(capturedOldState, result)
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

    /**
     * テンキー/英数/abbrevモード
     * @return 処理した場合は true
     */
    private fun asciiOutput(
        key: String,
        state: KeyboardState,
    ): Boolean {
        val outChar = if (state.shiftState == ShiftState.LOWERCASE) key else key.uppercase()
        val firstChar = outChar.firstOrNull() ?: return false

        when (state.inputMode) {
            InputMode.HALF_ASCII -> {
                inputCommitter.commit(outChar)
                return true
            }
            InputMode.FULL_ASCII -> {
                val fullChar = when (val code = firstChar.code) {
                    /* Space */
                    0x20 -> FULL_SPACE
                    /* !から~までの半角英数字 */
                    in 0x21..0x7E -> (code + 0xFEE0).toChar().toString()
                    else -> outChar
                }
                inputCommitter.commit(fullChar)
                return true
            }
            else -> {
                if (state.currentSkkState.skkMode == SkkMode.ABBREV) {
                    stateFlow.update {
                        it.copy(
                            currentSkkState = it.currentSkkState.copy(
                                midashiText = it.currentSkkState.midashiText + outChar
                            )
                        )
                    }
                    return true
                }
            }
        }

        return false
    }
}