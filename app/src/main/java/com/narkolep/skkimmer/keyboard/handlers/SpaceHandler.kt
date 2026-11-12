package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.data.DictionaryManager
import com.narkolep.skkimmer.keyboard.KeyProcessor
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.KeyboardState
import com.narkolep.skkimmer.keyboard.ShiftState
import com.narkolep.skkimmer.keyboard.SkkConstants.DICT_DELIMITER
import com.narkolep.skkimmer.keyboard.SkkConstants.FULL_SPACE
import com.narkolep.skkimmer.keyboard.SkkConstants.HALF_SPACE
import com.narkolep.skkimmer.keyboard.clear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Spaceキーが押されたときの処理
 */
fun spaceHandler(
    stateFlow: MutableStateFlow<KeyboardState>,
    dictionaryManager: DictionaryManager,
    keyProcessor: KeyProcessor,
    coroutineScope: CoroutineScope
) {
    val state = stateFlow.value

    when (state.skkState) {
        SkkState.NORMAL -> {
            if (state.tourokuFlag.isNotEmpty()) {
                stateFlow.update {
                    it.copy(
                        tourokuFlag = it.tourokuFlag + HALF_SPACE
                    )
                }
                return
            }

            if (state.shiftState == ShiftState.LOWERCASE) {
                /* 半角スペース */
                keyProcessor.handle(HALF_SPACE)
            } else {
                /* 全角スペース */
                keyProcessor.handle(FULL_SPACE)
            }
        }

        SkkState.MIDASHI -> {
            if (state.midashiText.isEmpty()) return

            /* 連結処理を実行 */
            keyProcessor.handle("")

            getCandidatesAndChangeState(stateFlow, dictionaryManager, coroutineScope)
        }

        SkkState.OKURIGANA -> {
            if (state.okuriganaText.isEmpty()) return

            getCandidatesAndChangeState(stateFlow, dictionaryManager, coroutineScope)
        }

        SkkState.HENKAN -> {
            if (state.candidates.isEmpty()) return

            if (state.selectedIndex < state.candidates.size - 1) {
                stateFlow.update {
                    it.copy(
                        selectedIndex = it.selectedIndex + 1
                    )
                }
                return
            }

            /* 既に登録モードになっているとき(再帰的な辞書登録はしない) */
            if (state.tourokuFlag.isNotEmpty()) return

            /* 登録モードではないとき、登録モードに入る */
            stateFlow.update {
                it.clear().copy(
                    oldSkkState = it.skkState,
                    oldMidashiText = it.midashiText,
                    oldOkuriganaText = it.okuriganaText,
                    oldOkuriganaTrigger = it.okuriganaTrigger,
                    tourokuFlag = "[登録]" + it.midashiText + it.okuriganaTrigger + DICT_DELIMITER
                )
            }
        }

        SkkState.ABBREV -> {
            if (state.midashiText.isEmpty()) return

            getCandidatesAndChangeState(stateFlow, dictionaryManager, coroutineScope)
        }
    }
}