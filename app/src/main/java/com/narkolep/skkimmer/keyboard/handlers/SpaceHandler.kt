package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.data.SkkDictionaryManager
import com.narkolep.skkimmer.keyboard.KeyProcessor
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.clear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SpaceHandler(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val keyProcessor: KeyProcessor,
    private val dictionaryManager: SkkDictionaryManager
) {
    fun handle() {
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
                /* 連結処理をしてから変換 */
                keyProcessor.handle("")

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

            SkkState.OKURIGANA -> {
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

            SkkState.HENKAN -> {
                if (state.candidates.isNotEmpty()) {
                    if (state.selectedIndex < state.candidates.size - 1) {
                        stateFlow.update {
                            it.copy(
                                selectedIndex = state.selectedIndex + 1
                            )
                        }
                        return
                    }

                    if (state.tourokuFlag.isEmpty()) {
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
                }
            }

            SkkState.ABBREV -> {
                CoroutineScope(Dispatchers.Main).launch {
                    val candidatesList = dictionaryManager.getCandidates(state.midashiText)

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
                                oldOkuriganaText = "",
                                oldOkuriganaTrigger = "",
                                tourokuFlag = "[登録]" + state.midashiText + ":"
                            )
                        }
                    }
                }
            }
        }
    }
}