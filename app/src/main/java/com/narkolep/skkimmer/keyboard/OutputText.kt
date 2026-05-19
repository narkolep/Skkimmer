package com.narkolep.skkimmer.keyboard

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class OutputText(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val inputCommitter: InputCommitter,
    private val composingManager: ComposingManager
) {
    /**
     * テンキー/英数/abbrevモード (直接入力)
     **/
    fun asciiOutput(
        key: String,
        state: SkkUIState
    ): Boolean {
        val outChar = if (state.shiftState == ShiftState.LOWERCASE) key else key.uppercase()

        when (state.inputMode) {
            InputMode.HALF_ASCII, InputMode.NUMERIC -> {
                inputCommitter.commit(outChar)
                return true
            }
            InputMode.FULL_ASCII -> {
                val fullChar = when (outChar.first().code) {
                    /* Space */
                    0x20 -> "\u3000"
                    /* !から~までの半角英数字 */
                    in 0x21..0x7E -> (outChar.first().code + 0xFEE0).toChar().toString()
                    else -> outChar
                }
                inputCommitter.commit(fullChar)
                return true
            }
            else -> {
                if (state.skkState == SkkState.ABBREV) {
                    stateFlow.update { it.copy(midashiText = state.midashiText + outChar) }
                    return true
                }
            }
        }

        return false
    }

    /**
     * ConvertResultの値をstateに保存
     * 仮名を出力
     **/
    fun kanaOutput(
        newState: SkkUIState,
        oldState: SkkUIState,
        result: ConvertResult
    ) {
        stateFlow.update {
            it.copy(
                composingText = result.composingNext,
                oldOkuriganaTrigger = result.okuriganaFlag
            )
        }

        when (newState.skkState) {
            SkkState.NORMAL -> {
                if (newState.tourokuFlag.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            tourokuFlag = newState.tourokuFlag + result.output
                        )
                    }
                } else {
                    inputCommitter.commit(result.output)
                }
            }

            SkkState.MIDASHI -> {
                stateFlow.update { it.copy(
                    midashiText = newState.midashiText + result.output,
                ) }
            }

            SkkState.OKURIGANA -> {
                if (result.output.isEmpty() || result.isIgnore) {
                    /* outputが"ん"、"っ"、もしくは未確定のとき */
                    stateFlow.update {
                        it.copy(
                            midashiText = newState.midashiText + if (oldState.skkState == SkkState.MIDASHI) result.output else "",
                            okuriganaText = newState.okuriganaText + if (oldState.skkState == SkkState.OKURIGANA) result.output else "",
                        )
                    }
                } else {
                    /* 候補を検索する */
                    var flag = result.okuriganaFlag
                    if (oldState.okuriganaText.firstOrNull() == 'ん' || oldState.okuriganaText.firstOrNull() == 'ン') flag = "n"
                    if (oldState.okuriganaText.firstOrNull() == 'っ' || oldState.okuriganaText.firstOrNull() == 'ッ') flag = "t"

                    stateFlow.update {
                        it.copy(
                            okuriganaText = newState.okuriganaText + result.output,
                            okuriganaTrigger = flag
                        )
                    }

                    val state = stateFlow.value
                    composingManager.handleDictionaryManager(state)
                }
            }

            SkkState.HENKAN -> {
                /* 変換中に文字入力があったら、確定する */
                stateFlow.update { it.copy(okuriganaText = newState.okuriganaText + result.output) }
                composingManager.commit()
                stateFlow.update { it.copy(composingText = result.composingNext) }
            }
            else -> {}
        }
    }
}