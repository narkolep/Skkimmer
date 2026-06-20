package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.data.DictionaryManager
import com.narkolep.skkimmer.keyboard.KeyProcessor
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.clear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Spaceキーが押されたときの処理
 */
fun spaceHandler(
    stateFlow: MutableStateFlow<SkkUIState>,
    dictionaryManager: DictionaryManager,
    keyProcessor: KeyProcessor
) {
    val state = stateFlow.value

    when (state.skkState) {
        SkkState.NORMAL -> {
            if (state.tourokuFlag.isNotEmpty()) {
                stateFlow.update {
                    it.copy(
                        tourokuFlag = state.tourokuFlag + " "
                    )
                }
                return
            }

            keyProcessor.handle(" ")
        }

        SkkState.MIDASHI -> {
            if (state.midashiText.isEmpty()) return

            /* 連結処理を実行 */
            keyProcessor.handle("")

            getCandidatesAndChangeState(stateFlow, dictionaryManager)
        }

        SkkState.OKURIGANA -> {
            if (state.okuriganaText.isEmpty()) return

            getCandidatesAndChangeState(stateFlow, dictionaryManager)
        }

        SkkState.HENKAN -> {
            if (state.candidates.isEmpty()) return

            if (state.selectedIndex < state.candidates.size - 1) {
                stateFlow.update {
                    it.copy(
                        selectedIndex = state.selectedIndex + 1
                    )
                }
                return
            }

            /* 既に登録モードになっているとき */
            if (state.tourokuFlag.isNotEmpty()) return

            /* 登録モードではないとき、登録モードに入る */
            stateFlow.update { it.clear() }
            stateFlow.update {
                it.copy(
                    oldMidashiText = state.midashiText,
                    oldOkuriganaText = state.okuriganaText,
                    oldOkuriganaTrigger = state.okuriganaTrigger,
                    tourokuFlag = "[登録]" + state.midashiText + state.okuriganaTrigger + ":"
                )
            }
        }

        SkkState.ABBREV -> {
            if (state.midashiText.isEmpty()) return

            getCandidatesAndChangeState(stateFlow, dictionaryManager)
        }
    }
}