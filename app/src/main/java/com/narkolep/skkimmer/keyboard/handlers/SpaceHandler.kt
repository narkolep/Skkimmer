package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.ActionProcessor
import com.narkolep.skkimmer.keyboard.SkkMode
import com.narkolep.skkimmer.keyboard.ShiftState
import com.narkolep.skkimmer.keyboard.SkkConstants.FULL_SPACE
import com.narkolep.skkimmer.keyboard.SkkConstants.HALF_SPACE
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

            outputManager.getCandidatesAndChangeState()
        }

        SkkMode.HENKAN -> {
            if (state.candidates.isEmpty()) return

            if (state.selectedIndex < state.candidates.size - 1) {
                stateFlow.update {
                    it.copy(
                        selectedIndex = it.selectedIndex + 1
                    )
                }
                return
            }

//            /* 既に登録モードになっているとき(再帰的な辞書登録はしない) */
//            if (state.isRegistering) return

            /* 登録モードではないとき、登録モードに入る */
            val key = formatString(
                text = state.currentSkkState.midashiText + state.currentSkkState.okuriganaTrigger
            )
            stateFlow.update {
                it.initRegistration(key.lookupKey)
            }
        }

        SkkMode.ABBREV -> {
            if (state.currentSkkState.midashiText.isEmpty()) return

            outputManager.getCandidatesAndChangeState()
        }
    }
}