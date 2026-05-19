package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.data.DictionaryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ComposingManager(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val inputCommitter: InputCommitter,
    private val dictionaryManager: DictionaryManager
) {
    /**
     * 表示する文字列の更新
     */
    fun update() {
        val state = stateFlow.value

        val displayText = when (state.skkState) {
            SkkState.NORMAL -> {
                if (state.composingText.isEmpty() && state.tourokuFlag.isEmpty()) {
                    inputCommitter.commit("")
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

        inputCommitter.setComposingText(displayText)
    }

    /**
     * 文字列の確定
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
                    dictionaryManager.learnWord(learnText, state.candidates[state.selectedIndex], true)
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

    /**
     * 辞書の検索、モード切り換え
     */
    fun handleDictionaryManager(state: SkkUIState) {
        val keyText = state.midashiText + state.okuriganaTrigger

        CoroutineScope(Dispatchers.Main).launch {
            val candidatesList = dictionaryManager.getCandidates(keyText)

            if (candidatesList.isNotEmpty()) {
                stateFlow.update {
                    it.copy(
                        skkState = SkkState.HENKAN,
                        candidates = candidatesList,
                        selectedIndex = 0
                    )
                }
            } else if (state.tourokuFlag.isEmpty()) {
                stateFlow.update { it.clear() }

                stateFlow.update {
                    it.copy(
                        oldMidashiText = state.midashiText,
                        oldOkuriganaText = state.okuriganaText,
                        oldOkuriganaTrigger = state.okuriganaTrigger,
                        tourokuFlag = "[登録]$keyText:"
                    )
                }
            }
        }
    }
}