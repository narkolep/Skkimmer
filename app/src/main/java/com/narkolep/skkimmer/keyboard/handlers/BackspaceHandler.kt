package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.ActionProcessor
import com.narkolep.skkimmer.keyboard.SkkMode
import com.narkolep.skkimmer.keyboard.SkkConstants.HALF_SPACE
import com.narkolep.skkimmer.keyboard.SkkState
import kotlinx.coroutines.flow.update

/**
 * backspaceキーの処理
 */
internal fun ActionProcessor.handleBackspace() {
    val state = stateFlow.value

    /* 範囲選択されているときは空文字を出力 */
    if (inputCommitter.isSelected()) {
        inputCommitter.deleteSelection()

        stateFlow.update { it.resetAll() }
        return
    }

    when (state.currentSkkState.skkMode) {
        SkkMode.NORMAL -> {
            val skkState = state.savedSkkState.lastOrNull() ?: SkkState()

            if (state.composingText.isNotEmpty()) {
                /* composingTextが存在するとき */
                stateFlow.update {
                    it.copy(
                        composingText = it.composingText.dropLast(1)
                    )
                }
                return
            }

            if (state.registrationKey.lookupKey.isEmpty()) {
                /* 未確定の文字列がないとき */
                inputCommitter.delete(1)
                return
            }

            if (state.registrationKey.candidate.isNotEmpty()) {
                /* 登録する文字列が存在するとき */
                stateFlow.update {
                    it.updateCandidate(
                        lookupKey = state.registrationKey.lookupKey,
                        candidate = state.registrationKey.candidate.dropLast(1)
                    )
                }
                return
            }

            /* 変換前の状態を復元する */
            stateFlow.update {
                it.copy(
                    composingText = if (skkState.skkMode == SkkMode.ABBREV) HALF_SPACE else "",
                    currentSkkState = skkState,
                    savedSkkState = it.savedSkkState.dropLast(1),
                    registrationKeyList = it.registrationKeyList.dropLast(1)
                )
            }
        }

        SkkMode.MIDASHI -> {
            /* " "のとき */
            if (state.composingText == HALF_SPACE) {
                stateFlow.update { it.clear() }
                return
            }

            /* composingTextが存在するとき */
            if (state.composingText.isNotEmpty()) {
                stateFlow.update {
                    val newComposingText = it.composingText.dropLast(1)

                    it.copy(
                        composingText =
                            if (newComposingText.isEmpty() && it.currentSkkState.midashiText.isEmpty()) HALF_SPACE
                            else newComposingText
                    )
                }
                return
            }

            /* 見出し文字列が存在するとき */
            if (state.currentSkkState.midashiText.isNotEmpty()) {
                stateFlow.update {
                    it.copy(
                        composingText =
                            if (it.currentSkkState.midashiText.dropLast(1).isEmpty()) HALF_SPACE
                            else it.composingText,
                        currentSkkState = it.currentSkkState.copy(
                            midashiText = it.currentSkkState.midashiText.dropLast(1)
                        )
                    )
                }
                return
            }

            /* 見出し文字列が空のとき */
            stateFlow.update { it.clear() }
        }

        SkkMode.OKURIGANA -> {
            /* composingTextが存在するとき */
            if (state.composingText.isNotEmpty()) {
                stateFlow.update {
                    it.copy(
                        composingText = it.composingText.dropLast(1)
                    )
                }
                return
            }

            /* 送り仮名が存在するとき */
            if (state.currentSkkState.okuriganaText.isNotEmpty()) {
                stateFlow.update {
                    it.copy(
                        currentSkkState = it.currentSkkState.copy(
                            okuriganaText = it.currentSkkState.okuriganaText.dropLast(1)
                        )
                    )
                }
                return
            }

            /* 送り仮名が存在しないとき */
            stateFlow.update {
                it.copy(
                    candidates = emptyList(),
                    selectedIndex = -1,
                    currentSkkState = it.currentSkkState.copy(
                        skkMode = SkkMode.MIDASHI,
                        okuriganaTrigger = ""
                    )
                )
            }
        }

        SkkMode.HENKAN -> {
            val skkState = state.savedSkkState.lastOrNull() ?: SkkState()

            /* 変換前の状態を復元する */
            stateFlow.update {
                it.copy(
                    composingText = if (skkState.skkMode == SkkMode.ABBREV) HALF_SPACE else "",
                    candidates = emptyList(),
                    selectedIndex = -1,
                    currentSkkState = if (skkState.skkMode == SkkMode.OKURIGANA) {
                        skkState.copy(
                            okuriganaText = skkState.okuriganaText.dropLast(1)
                        )
                    } else {
                        skkState
                    },
                    savedSkkState = it.savedSkkState.dropLast(1)
                )
            }
        }

        SkkMode.ABBREV -> {
            /* 見出し文字列が存在するとき */
            if (state.currentSkkState.midashiText.isNotEmpty()) {
                stateFlow.update {
                    it.copy(
                        currentSkkState = it.currentSkkState.copy(
                            midashiText = it.currentSkkState.midashiText.dropLast(1)
                        )
                    )
                }
                return
            }

            /* 見出し文字列が存在しないとき */
            stateFlow.update { it.clear() }
        }
    }
}