package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.InputCommitter
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.clear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class BackspaceHandler(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val inputCommitter: InputCommitter
) {
    fun handle() {
        val state = stateFlow.value

        /* 範囲選択されているときは空文字を出力 */
        if (inputCommitter.isSelected()) {
            inputCommitter.commit("")
            return
        }

        when (state.skkState) {
            SkkState.NORMAL -> {
                if (state.composingText.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            composingText = state.composingText.dropLast(1)
                        )
                    }
                    return
                }

                if (state.tourokuFlag.isNotEmpty()) {
                    if (state.tourokuFlag.substringAfter(":").isNotEmpty()) {
                        stateFlow.update {
                            it.copy(
                                tourokuFlag = state.tourokuFlag.dropLast(1)
                            )
                        }
                        return
                    }

                    if (state.oldOkuriganaTrigger.isNotEmpty()) {
                        stateFlow.update {
                            it.copy(
                                skkState = SkkState.OKURIGANA,
                                composingText = "",
                                midashiText = state.oldMidashiText,
                                okuriganaText = state.oldOkuriganaText,
                                okuriganaTrigger = state.oldOkuriganaTrigger,
                                oldMidashiText = "",
                                oldOkuriganaText = "",
                                oldOkuriganaTrigger = "",
                                tourokuFlag = ""
                            )
                        }
                        return
                    }

                    /* 登録文字列も送り仮名もないとき、見出しモードに戻る */
                    stateFlow.update {
                        it.copy(
                            skkState = SkkState.MIDASHI,
                            composingText = "",
                            midashiText = state.oldMidashiText,
                            okuriganaText = state.oldOkuriganaText,
                            okuriganaTrigger = state.oldOkuriganaTrigger,
                            oldMidashiText = "",
                            oldOkuriganaText = "",
                            oldOkuriganaTrigger = "",
                            tourokuFlag = ""
                        )
                    }
                    return
                }

                /* 未確定の文字列がないとき */
                inputCommitter.delete()
            }

            SkkState.MIDASHI -> {
                if (state.composingText.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            composingText = state.composingText.dropLast(1)
                        )
                    }
                    return
                }

                if (state.midashiText.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            midashiText = state.midashiText.dropLast(1)
                        )
                    }
                    return
                }

                /* 見出し文字列がなければ NORMAL に戻す */
                stateFlow.update { it.clear() }
            }

            SkkState.OKURIGANA -> {
                if (state.composingText.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            composingText = state.composingText.dropLast(1)
                        )
                    }
                    return
                }

                if (state.okuriganaText.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            okuriganaText = state.okuriganaText.dropLast(1)
                        )
                    }
                    return
                }

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
                    return
                }

                stateFlow.update {
                    it.copy(
                        skkState = SkkState.MIDASHI,
                        candidates = emptyList(),
                        selectedIndex = -1
                    )
                }
            }

            SkkState.ABBREV -> {
                if (state.midashiText.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            midashiText = state.midashiText.dropLast(1)
                        )
                    }
                    return
                }

                stateFlow.update { it.clear() }
            }
        }
    }
}