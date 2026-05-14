package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.InputMode
import com.narkolep.skkimmer.keyboard.ShiftState
import com.narkolep.skkimmer.keyboard.SkkUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Modifierキーの処理をまとめたclass
 **/
class ModifierHandler(
    private val stateFlow: MutableStateFlow<SkkUIState>
) {
    /**
     * Shiftキーが押されたときの処理
     **/
    fun handleShift() {
        val state = stateFlow.value
        val now = System.currentTimeMillis().toInt()
        val timing = now - state.lastShiftPressTime < 500

        val nextShift = when (state.shiftState) {
            ShiftState.LOWERCASE -> ShiftState.SHIFTED
            ShiftState.SHIFTED -> {
                if (timing && (state.inputMode == InputMode.HALF_ASCII || state.inputMode == InputMode.FULL_ASCII))
                    ShiftState.CAPS_LOCK
                else
                    ShiftState.LOWERCASE
            }
            ShiftState.CAPS_LOCK -> ShiftState.LOWERCASE
        }

        stateFlow.update {
            it.copy(
                lastShiftPressTime = now,
                shiftState = nextShift
            )
        }
    }

    /**
     * Ctrlキーが押されたときの処理
     **/
    fun handleCtrl() {
        val state = stateFlow.value

        stateFlow.update {
            it.copy(
                isCtrlPressed = !state.isCtrlPressed
            )
        }
    }
}