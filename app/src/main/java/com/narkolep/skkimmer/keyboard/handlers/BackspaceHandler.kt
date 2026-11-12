package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.InputCommitter
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.KeyboardState
import com.narkolep.skkimmer.keyboard.SkkConstants.DICT_DELIMITER
import com.narkolep.skkimmer.keyboard.SkkConstants.HALF_SPACE
import com.narkolep.skkimmer.keyboard.clear
import com.narkolep.skkimmer.keyboard.resetAll
import com.narkolep.skkimmer.keyboard.tourokuClear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * 文字を削除する
 */
fun backspaceHandler(
    stateFlow : MutableStateFlow<KeyboardState>,
    inputCommitter: InputCommitter
) {
    val state = stateFlow.value

    /* 範囲選択されているときは空文字を出力 */
    if (inputCommitter.isSelected()) {
        inputCommitter.commit("")

        stateFlow.update { it.resetAll() }
        return
    }

    when (state.skkState) {
        SkkState.NORMAL -> {
            if (state.composingText.isNotEmpty()) {
                /* composingTextが存在するとき */
                stateFlow.update {
                    it.copy(
                        composingText = it.composingText.dropLast(1)
                    )
                }
                return
            }

            if (state.tourokuFlag.isEmpty()) {
                /* 未確定の文字列がないとき */
                inputCommitter.delete(1)
                return
            }

            if (state.tourokuFlag.substringAfter(DICT_DELIMITER).isNotEmpty()) {
                /* 登録する文字列が存在するとき */
                stateFlow.update {
                    it.copy(
                        tourokuFlag = it.tourokuFlag.dropLast(1)
                    )
                }
                return
            }

            if (state.oldSkkState == SkkState.OKURIGANA) {
                /* 登録する文字列が空で、送り仮名があるとき */
                stateFlow.update {
                    it.tourokuClear().copy(
                        skkState = SkkState.OKURIGANA,
                        composingText = "",
                        midashiText = it.oldMidashiText,
                        okuriganaText = it.oldOkuriganaText,
                        okuriganaTrigger = it.oldOkuriganaTrigger,
                    )
                }
                return
            }

            if (state.oldSkkState == SkkState.ABBREV) {
                /* 登録する文字列が空で、ABBREVモードから変換したとき */
                stateFlow.update {
                    it.tourokuClear().copy(
                        skkState = SkkState.ABBREV,
                        composingText = HALF_SPACE,
                        midashiText = it.oldMidashiText,
                        okuriganaText = "",
                        okuriganaTrigger = ""
                    )
                }
                return
            }

            /* OKURIGANAでもABBREVでもないとき */
            stateFlow.update {
                it.tourokuClear().copy(
                    skkState = SkkState.MIDASHI,
                    composingText = "",
                    midashiText = it.oldMidashiText,
                    okuriganaText = "",
                    okuriganaTrigger = ""
                )
            }
        }

        SkkState.MIDASHI -> {
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
                            if (newComposingText.isEmpty() && it.midashiText.isEmpty()) HALF_SPACE
                            else newComposingText
                    )
                }
                return
            }

            /* 見出し文字列が存在するとき */
            if (state.midashiText.isNotEmpty()) {
                stateFlow.update {
                    val newMidashiText = it.midashiText.dropLast(1)

                    it.copy(
                        midashiText = newMidashiText,
                        composingText =
                            if (newMidashiText.isEmpty()) HALF_SPACE
                            else it.composingText
                    )
                }
                return
            }

            /* 見出し文字列が空のとき */
            stateFlow.update { it.clear() }
        }

        SkkState.OKURIGANA -> {
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
            if (state.okuriganaText.isNotEmpty()) {
                stateFlow.update {
                    it.copy(
                        okuriganaText = it.okuriganaText.dropLast(1)
                    )
                }
                return
            }

            /* 送り仮名が存在しないとき */
            stateFlow.update {
                it.copy(
                    skkState = SkkState.MIDASHI,
                    okuriganaTrigger = "",
                    candidates = emptyList(),
                    selectedIndex = -1
                )
            }
        }

        SkkState.HENKAN -> {
            /* 送り仮名があるとき */
            if (state.previousSkkState == SkkState.OKURIGANA) {
                stateFlow.update {
                    it.copy(
                        skkState = SkkState.OKURIGANA,
                        okuriganaText = it.okuriganaText.dropLast(1),
                        composingText = "",
                        candidates = emptyList(),
                        selectedIndex = -1
                    )
                }
                return
            }

            /* 見出し文字列が半角英数字のみのとき */
            if (state.previousSkkState == SkkState.ABBREV) {
                stateFlow.update {
                    it.copy(
                        skkState = SkkState.ABBREV,
                        composingText = HALF_SPACE,
                        candidates = emptyList(),
                        selectedIndex = -1
                    )
                }
                return
            }

            /* OKURIGANAでもABBREVでもないとき */
            stateFlow.update {
                it.copy(
                    skkState = SkkState.MIDASHI,
                    candidates = emptyList(),
                    selectedIndex = -1
                )
            }
        }

        SkkState.ABBREV -> {
            /* 見出し文字列が存在するとき */
            if (state.midashiText.isNotEmpty()) {
                stateFlow.update {
                    it.copy(
                        midashiText = it.midashiText.dropLast(1)
                    )
                }
                return
            }

            /* 見出し文字列が存在しないとき */
            stateFlow.update { it.clear() }
        }
    }
}