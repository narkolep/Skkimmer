package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.ActionProcessor
import com.narkolep.skkimmer.keyboard.SkkMode
import com.narkolep.skkimmer.keyboard.ShiftState
import com.narkolep.skkimmer.keyboard.SkkConstants.FULL_SPACE
import com.narkolep.skkimmer.keyboard.SkkConstants.HALF_SPACE
import com.narkolep.skkimmer.keyboard.formatString
import kotlinx.coroutines.flow.update

/**
 * Spaceキーが押されたときの処理
 */
internal fun ActionProcessor.handleSpace() {
    val state = stateFlow.value

    when (state.currentSkkState.skkMode) {
        SkkMode.NORMAL -> {
            val space = if (state.shiftState == ShiftState.LOWERCASE) HALF_SPACE else FULL_SPACE

            keyProcessor.handle(space)
        }

        SkkMode.MIDASHI -> {
            if (state.currentSkkState.midashiText.isEmpty()) return

            /* 連結処理を実行 */
            keyProcessor.handle("")

            /* 候補を取得してstateに保存する */
            outputManager.getCandidatesAndChangeState()
        }

        SkkMode.OKURIGANA -> {
            if (state.currentSkkState.okuriganaText.isEmpty()) return
            if (state.candidates.isEmpty()) {
                outputManager.getCandidatesAndChangeState()
                return
            }

            if (state.selectedIndex < state.candidates.size - 1) {
                stateFlow.update { it
                    .changeMode(
                        mode = SkkMode.HENKAN
                    )
                    .copy(
                        savedSkkState = it.savedSkkState + it.currentSkkState,
                        selectedIndex = it.selectedIndex + 1,
                    )
                }
                return
            }

            initRegistration()
        }

        SkkMode.HENKAN -> {
            if (state.candidates.isEmpty()) {
                outputManager.getCandidatesAndChangeState()
                return
            }

            if (state.selectedIndex < state.candidates.size - 1) {
                stateFlow.update {
                    it.copy(
                        selectedIndex = it.selectedIndex + 1
                    )
                }
                return
            }

            initRegistration()
        }

        SkkMode.ABBREV -> {
            if (state.currentSkkState.midashiText.isEmpty()) return

            outputManager.getCandidatesAndChangeState()
        }
    }
}

private fun ActionProcessor.initRegistration() {
    /* 既に登録モードになっているとき(再帰的な辞書登録はしない) */
    if (stateFlow.value.isRegistering) return

    /* 登録モードではないとき、登録モードに入る */
    val key = formatString(
        text = stateFlow.value.currentSkkState.midashiText + stateFlow.value.currentSkkState.okuriganaKey
    )
    stateFlow.update {
        it.initRegistration(key.lookupKey)
    }
}