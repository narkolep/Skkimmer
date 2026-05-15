package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.data.SkkDictionaryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OutputText(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val inputCommitter: InputCommitter,
    private val composingManager: ComposingManager,
    private val dictionaryManager: SkkDictionaryManager
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
                val fullChar =
                    if (outChar.first().code in 0x21..0x7E) (outChar.first().code + 0xFEE0).toChar().toString()
                    else outChar
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
        stateFlow.update { it.copy(
            composingText = result.composingNext,
            oldOkuriganaTrigger = result.okuriganaFlag
        ) }

        when (newState.skkState) {
            SkkState.NORMAL -> {
                if (newState.tourokuFlag.isNotEmpty()) {
                    stateFlow.update { it.copy(
                        tourokuFlag = newState.tourokuFlag + result.output
                    ) }
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
                    /* outputText == "ん" もしくは 未確定 */
                    stateFlow.update { it.copy(
                        midashiText = newState.midashiText + if (oldState.skkState == SkkState.MIDASHI) result.output else "",
                        okuriganaText = newState.okuriganaText + if (oldState.skkState == SkkState.OKURIGANA) result.output else "",
                    ) }
                } else {
                    /* 仮名(ん,っ,以外)が出力されたとき、候補を検索する */
                    var flag = result.okuriganaFlag
                    if (oldState.okuriganaText.firstOrNull() == 'ん' || oldState.okuriganaText.firstOrNull() == 'ン') flag = "n"
                    if (oldState.okuriganaText.firstOrNull() == 'っ' || oldState.okuriganaText.firstOrNull() == 'ッ') flag = "t"

                    stateFlow.update { it.copy(
                        okuriganaText = newState.okuriganaText + result.output,
                        okuriganaTrigger = flag
                    ) }

                    val keyText = newState.midashiText + flag

                    CoroutineScope(Dispatchers.Main).launch {
                        val candidatesList = dictionaryManager.getCandidates(keyText)
                        if (candidatesList.isNotEmpty()) {
                            stateFlow.update { it.copy(
                                skkState = SkkState.HENKAN,
                                candidates = candidatesList,
                                selectedIndex = 0
                            ) }
                        } else {
                            /* 候補が無ければ、辞書登録モードに移行 */
                            stateFlow.update { it.copy(
                                skkState = SkkState.NORMAL,
                                composingText = "",
                                midashiText = "",
                                okuriganaText = "",
                                oldOkuriganaTrigger = flag,
                                tourokuText = keyText,
                                tourokuFlag = "[登録]$keyText:"
                            ) }
                        }
                    }
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