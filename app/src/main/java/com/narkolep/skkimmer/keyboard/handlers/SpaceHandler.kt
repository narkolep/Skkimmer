package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.ComposingManager
import com.narkolep.skkimmer.keyboard.KeyProcessor
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.clear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class SpaceHandler(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val keyProcessor: KeyProcessor,
    private val composingManager: ComposingManager
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
                if (state.midashiText.isEmpty()) return

                /* 連結処理を実行 */
                keyProcessor.handle("")
                /* stateを再取得 */
                val state = stateFlow.value

                composingManager.handleDictionaryManager(state)
            }

            SkkState.OKURIGANA -> {
                if (state.okuriganaText.isEmpty()) return

                composingManager.handleDictionaryManager(state)
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

            SkkState.ABBREV -> {
                if (state.midashiText.isEmpty()) return

                composingManager.handleDictionaryManager(state)
            }
        }
    }
}