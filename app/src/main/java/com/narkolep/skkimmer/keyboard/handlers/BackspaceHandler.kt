package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.InputCommitter
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.clear
import com.narkolep.skkimmer.keyboard.tourokuClear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class BackspaceHandler(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val inputCommitter: InputCommitter
) {
    /**
     * 文字を削除する
     * @return 削除した文字を返す
     */
    fun handle(): String {
        val state = stateFlow.value

        /* 範囲選択されているときは空文字を出力 */
        if (inputCommitter.isSelected()) {
            inputCommitter.commit("")

            stateFlow.update { it.tourokuClear() }
            stateFlow.update { it.clear() }
            return ""
        }

        when (state.skkState) {
            SkkState.NORMAL -> {
                if (state.composingText.isNotEmpty()) {
                    /* composingTextが存在するとき */
                    stateFlow.update {
                        it.copy(
                            composingText = state.composingText.dropLast(1)
                        )
                    }
                    return ""
                }

                if (state.tourokuFlag.isEmpty()) {
                    /* 未確定の文字列がないとき */
                    val text = inputCommitter.getText(1).toString()
                    inputCommitter.delete()
                    return text
                }

                if (state.tourokuFlag.substringAfter(":").isNotEmpty()) {
                    /* 登録する文字列が存在するとき */
                    stateFlow.update {
                        it.copy(
                            tourokuFlag = state.tourokuFlag.dropLast(1)
                        )
                    }
                    return state.tourokuFlag.takeLast(1)
                }

                if (state.oldOkuriganaTrigger.isNotEmpty()) {
                    /* 登録する文字列が空で、送り仮名があるとき */
                    stateFlow.update { it.tourokuClear() }
                    stateFlow.update {
                        it.copy(
                            skkState = SkkState.OKURIGANA,
                            composingText = "",
                            midashiText = state.oldMidashiText,
                            okuriganaText = state.oldOkuriganaText,
                            okuriganaTrigger = state.oldOkuriganaTrigger,
                        )
                    }
                    return ""
                }

                if (state.oldMidashiText.all { it.code in 0x21..0x7E }) {
                    /* 登録する文字列が空で、ABBREVモードから変換したとき */
                    stateFlow.update { it.tourokuClear() }
                    stateFlow.update {
                        it.copy(
                            skkState = SkkState.ABBREV,
                            composingText = " ", // 半角スペース
                            midashiText = state.oldMidashiText,
                            okuriganaText = "",
                            okuriganaTrigger = ""
                        )
                    }
                    return ""
                }

                /* OKURIGANAでもABBREVでもないとき */
                stateFlow.update { it.tourokuClear() }
                stateFlow.update {
                    it.copy(
                        skkState = SkkState.MIDASHI,
                        composingText = "",
                        midashiText = state.oldMidashiText,
                        okuriganaText = "",
                        okuriganaTrigger = ""
                    )
                }
                return ""
            }

            SkkState.MIDASHI -> {
                if (state.composingText.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            composingText = state.composingText.dropLast(1)
                        )
                    }
                    return ""
                }

                if (state.midashiText.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            midashiText = state.midashiText.dropLast(1)
                        )
                    }
                    return state.midashiText.takeLast(1)
                }

                /* 見出し文字列がなければ NORMAL に戻す */
                stateFlow.update { it.clear() }
                return ""
            }

            SkkState.OKURIGANA -> {
                if (state.composingText.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            composingText = state.composingText.dropLast(1)
                        )
                    }
                    return ""
                }

                /* 送り仮名が存在するとき */
                if (state.okuriganaText.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            okuriganaText = state.okuriganaText.dropLast(1)
                        )
                    }
                    return state.okuriganaText.takeLast(1)
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
                return ""
            }

            SkkState.HENKAN -> {
                if (state.okuriganaText.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            skkState = SkkState.OKURIGANA,
                            okuriganaText = state.okuriganaText.dropLast(1),
                            composingText = "",
                            candidates = emptyList(),
                            selectedIndex = -1
                        )
                    }
                    return state.okuriganaText.takeLast(1)
                }

                if (state.midashiText.all { it.code in 0x21..0x7E }) {
                    stateFlow.update {
                        it.copy(
                            skkState = SkkState.ABBREV,
                            composingText = " ", // 半角スペース
                            candidates = emptyList(),
                            selectedIndex = -1
                        )
                    }
                    return ""
                }

                stateFlow.update {
                    it.copy(
                        skkState = SkkState.MIDASHI,
                        candidates = emptyList(),
                        selectedIndex = -1
                    )
                }
                return ""
            }

            SkkState.ABBREV -> {
                if (state.midashiText.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            midashiText = state.midashiText.dropLast(1)
                        )
                    }
                    return state.midashiText.takeLast(1)
                }

                stateFlow.update { it.clear() }
                return ""
            }
        }
    }
}