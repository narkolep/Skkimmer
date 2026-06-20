package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.data.DictionaryManager
import com.narkolep.skkimmer.keyboard.handlers.getCandidatesAndChangeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OutputManager(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val inputCommitter: InputCommitter,
    private val dictionaryManager: DictionaryManager
) {
    /**
     * 確定入力
     * テンキー/英数/abbrevモード
     */
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
     */
    fun kanaOutput(
        newState: SkkUIState,
        oldState: SkkUIState,
        result: ConvertResult
    ) {
        stateFlow.update {
            it.copy(
                composingText = result.composingNext,
                oldOkuriganaTrigger =
                    if (it.tourokuFlag.isEmpty()) result.okuriganaFlag
                    else it.oldOkuriganaTrigger
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
                stateFlow.update {
                    it.copy(
                        midashiText = newState.midashiText + result.output,
                    )
                }
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

                    getCandidatesAndChangeState(stateFlow, dictionaryManager)
                }
            }

            SkkState.HENKAN -> {
                /* 変換中に文字入力があったら、確定する */
                stateFlow.update {
                    it.copy(
                        okuriganaText = newState.okuriganaText + result.output
                    )
                }
                commit()
                stateFlow.update {
                    it.copy(
                        composingText = result.composingNext
                    )
                }
            }

            SkkState.ABBREV -> {
                // abbrevの場合は連結処理がないので、何もしない
            }
        }
    }

    /**
     * 表示する未確定文字列の更新
     */
    fun update() {
        val state = stateFlow.value

        val midashiSymbol = "▽"
        val henkanSymbol = "▼"
        val okuriganaSymbol = "*"

        val displayText = when (state.skkState) {
            SkkState.NORMAL -> {
                if (state.composingText.isEmpty() && state.tourokuFlag.isEmpty()) {
                    inputCommitter.commit("")
                    return
                }
                state.tourokuFlag + state.composingText
            }
            SkkState.MIDASHI -> {
                state.tourokuFlag + midashiSymbol + state.midashiText + state.composingText
            }
            SkkState.OKURIGANA -> {
                state.tourokuFlag + midashiSymbol + state.midashiText + okuriganaSymbol + state.okuriganaText + state.composingText
            }
            SkkState.HENKAN -> {
                val displayOkuri =
                    if (state.okuriganaText.isNotEmpty()) okuriganaSymbol + state.okuriganaText + state.composingText
                    else ""
                val candidates = state.candidates[state.selectedIndex].split(";")[0]
                state.tourokuFlag + henkanSymbol + candidates + displayOkuri
            }
            SkkState.ABBREV -> {
                state.tourokuFlag + state.midashiText.ifEmpty { state.composingText }
            }
        }

        inputCommitter.setComposingText(displayText)
    }

    /**
     * 未確定文字列を確定する
     */
    fun commit() {
        val state = stateFlow.value

        val commitText = when (state.skkState) {
            SkkState.NORMAL -> {
                state.composingText
            }
            SkkState.MIDASHI -> {
                state.midashiText + state.composingText
            }
            SkkState.OKURIGANA -> {
                state.midashiText + state.composingText
            }
            SkkState.HENKAN -> {
                /* 履歴を記録 */
                val learnText = state.midashiText + state.okuriganaTrigger
                CoroutineScope(Dispatchers.IO).launch {
                    dictionaryManager.learnWord(
                        learnText,
                        state.candidates[state.selectedIndex],
                        true
                    )
                }

                val word = state.candidates[state.selectedIndex].split(";")[0]
                word + state.okuriganaText
            }
            SkkState.ABBREV -> {
                state.midashiText
            }
        }

        /* 状態をリセット */
        stateFlow.update {
            it.clear()
        }

        /* リセットした後、フラグを更新 */
        if (state.tourokuFlag.isEmpty()) {
            inputCommitter.setComposingText("")
            if (commitText.isNotEmpty()) inputCommitter.commit(commitText)
        } else {
            stateFlow.update { it.copy(tourokuFlag = state.tourokuFlag + commitText) }
        }
    }
}