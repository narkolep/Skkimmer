package com.narkolep.skkimmer.keyboard

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class ControlState(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val manager: OutputManager
) {
    fun changeState(
        key: String,
        state: SkkUIState
    ) {
        if (!(state.inputMode == InputMode.HIRAGANA || state.inputMode == InputMode.KATAKANA)) return
        if (state.shiftState == ShiftState.LOWERCASE) return
        if (!key.matches(Regex("^[a-z0-9]+$"))) return

        if (state.skkState == SkkState.NORMAL) {
            stateFlow.update { it.copy(
                skkState = SkkState.MIDASHI
            ) }
        }

        if (state.skkState == SkkState.MIDASHI && state.midashiText.isNotEmpty()) {
            stateFlow.update { it.copy(
                skkState = SkkState.OKURIGANA
            ) }
        }

        if (state.skkState == SkkState.HENKAN) {
            manager.commit()
            stateFlow.update { it.copy(
                skkState = SkkState.MIDASHI
            ) }
        }
    }
}